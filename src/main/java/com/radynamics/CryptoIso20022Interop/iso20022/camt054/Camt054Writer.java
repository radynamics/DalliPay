package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.IdGenerator;
import com.radynamics.CryptoIso20022Interop.iso20022.UUIDIdGenerator;
import com.radynamics.CryptoIso20022Interop.iso20022.Utils;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.schema.generated.*;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import org.apache.commons.lang3.NotImplementedException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Camt054Writer {
    private final Ledger ledger;
    private TransformInstruction transformInstruction;
    private CurrencyConverter ccyConverter;
    private IdGenerator idGenerator;
    private LocalDateTime creationDate;
    private DateFormat bookingDateFormat = DateFormat.DateTime;
    private DateFormat valutaDateFormat = DateFormat.DateTime;

    public Camt054Writer(Ledger ledger, TransformInstruction transformInstruction, CurrencyConverter ccyConverter) {
        this.ledger = ledger;
        this.transformInstruction = transformInstruction;
        this.ccyConverter = ccyConverter;
        this.idGenerator = new UUIDIdGenerator();
        this.creationDate = LocalDateTime.now();
    }

    public Document create(Transaction[] transactions) throws JAXBException, DatatypeConfigurationException {
        var d = new Document();

        d.setBkToCstmrDbtCdtNtfctn(new BankToCustomerDebitCreditNotificationV04());
        d.getBkToCstmrDbtCdtNtfctn().setGrpHdr(new GroupHeader58());
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setMsgId(idGenerator.createMsgId());
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setCreDtTm(Utils.toXmlDateTime(creationDate));
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setMsgRcpt(createMsgRcpt());
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setMsgPgntn(new Pagination());
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().getMsgPgntn().setPgNb("1");
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().getMsgPgntn().setLastPgInd(true);

        for (var t : transactions) {
            var receiver = t.getReceiver();
            var stmt = getNtfctnOrNull(d, receiver);
            if (stmt == null) {
                stmt = new AccountNotification7();
                d.getBkToCstmrDbtCdtNtfctn().getNtfctn().add(stmt);
                stmt.setId(idGenerator.createStmId());
                stmt.setElctrncSeqNb(BigDecimal.valueOf(0));
                stmt.setCreDtTm(Utils.toXmlDateTime(creationDate));
                stmt.setAcct(createAcct(receiver));
            }

            stmt.getNtry().add(createNtry(t));
            stmt.setElctrncSeqNb(stmt.getElctrncSeqNb().add(BigDecimal.ONE));
        }
        return d;
    }

    private AccountNotification7 getNtfctnOrNull(Document d, Wallet receiver) {
        for (var ntfctn : d.getBkToCstmrDbtCdtNtfctn().getNtfctn()) {
            if (CashAccountCompare.isSame(ntfctn.getAcct(), createAcct(receiver))) {
                return ntfctn;
            }
        }
        return null;
    }

    private CashAccount25 createAcct(Wallet receiver) {
        var acct = new CashAccount25();
        acct.setId(new AccountIdentification4Choice());
        var iban = transformInstruction.getIbanOrNull(receiver);
        if (iban == null) {
            acct.getId().setOthr(new GenericAccountIdentification1());
            acct.getId().getOthr().setId(receiver.getPublicKey());
        } else {
            acct.getId().setIBAN(iban.getUnformatted());
        }
        acct.setCcy(transformInstruction.getTargetCcy());

        return acct;
    }

    private PartyIdentification43 createMsgRcpt() {
        // see FI_camt_054_sample.xml.xml
        var o = new PartyIdentification43();

        o.setId(new Party11Choice());
        o.getId().setOrgId(new OrganisationIdentification8());

        var othr = new GenericOrganisationIdentification1();
        o.getId().getOrgId().getOthr().add(othr);
        othr.setId("CryptoIso20022Interop");
        othr.setSchmeNm(new OrganisationIdentificationSchemeName1Choice());
        othr.getSchmeNm().setCd("CUST");

        return o;
    }

    private ReportEntry4 createNtry(Transaction trx) throws DatatypeConfigurationException {
        var ntry = new ReportEntry4();

        // Seite 44: "Nicht standardisierte Verfahren: In anderen Fällen kann die «Referenz für den Kontoinhaber» geliefert werden."
        var iban = transformInstruction.getIbanOrNull(trx.getSender());
        ntry.setNtryRef(iban == null ? trx.getSender().getPublicKey() : iban.getUnformatted());
        ntry.setAmt(new ActiveOrHistoricCurrencyAndAmount());

        var amtValue = BigDecimal.ZERO;
        var amtCcy = "";
        if (trx.getCcy().equalsIgnoreCase(transformInstruction.getTargetCcy())) {
            amtValue = ledger.convertToNativeCcyAmount(trx.getAmountSmallestUnit());
            amtCcy = trx.getCcy();
        } else {
            var amt = ledger.convertToNativeCcyAmount(trx.getAmountSmallestUnit());
            var value = ccyConverter.convert(amt, trx.getCcy(), transformInstruction.getTargetCcy());
            // TODO: improve rounding (ex. JPY)
            amtValue = BigDecimal.valueOf(Math.round(value * 100.0) / 100.0);
            amtCcy = transformInstruction.getTargetCcy();
        }
        ntry.getAmt().setValue(amtValue);
        ntry.getAmt().setCcy(amtCcy);

        ntry.setCdtDbtInd(CreditDebitCode.CRDT);
        ntry.setSts(EntryStatus2Code.BOOK);

        var booked = Utils.toXmlDateTime(trx.getBooked());
        ntry.setBookgDt(createDateAndDateTimeChoice(booked, getBookingDateFormat()));
        ntry.setValDt(createDateAndDateTimeChoice(booked, getValutaDateFormat()));

        ntry.setBkTxCd(new BankTransactionCodeStructure4());
        ntry.getBkTxCd().setDomn(createDomn());

        var dtls = new EntryDetails3();
        ntry.getNtryDtls().add(dtls);
        dtls.setBtch(new BatchInformation2());
        dtls.getBtch().setNbOfTxs(String.valueOf(1));

        var txDtls = new EntryTransaction4();
        dtls.getTxDtls().add(txDtls);
        txDtls.setRefs(new TransactionReferences3());
        // Split due max allowed length of 35 per node (Max35Text)
        final int MAX_LEN = 35;
        var idPart0 = trx.getId().substring(0, MAX_LEN);
        var idPart1 = trx.getId().substring(MAX_LEN);
        txDtls.getRefs().setEndToEndId(idPart0);
        txDtls.getRefs().setMsgId(idPart1);

        txDtls.setAmt(new ActiveOrHistoricCurrencyAndAmount());
        txDtls.getAmt().setValue(amtValue);
        txDtls.getAmt().setCcy(amtCcy);
        txDtls.setCdtDbtInd(CreditDebitCode.CRDT);
        txDtls.setBkTxCd(new BankTransactionCodeStructure4());
        txDtls.getBkTxCd().setDomn(createDomn());

        var hasStrd = trx.getStructuredReferences() != null || trx.getInvoiceId() != null;
        if (hasStrd || trx.getMessages().length > 0) {
            txDtls.setRmtInf(new RemittanceInformation7());
        }

        if (hasStrd) {
            txDtls.getRmtInf().getStrd().add(createStrd(trx.getStructuredReferences(), trx.getInvoiceId()));
        }

        var sb = new StringBuilder();
        for (String s : trx.getMessages()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(s);
        }
        if (sb.length() > 0) {
            txDtls.getRmtInf().getUstrd().add(sb.toString());
        }

        return ntry;
    }

    private DateAndDateTimeChoice createDateAndDateTimeChoice(XMLGregorianCalendar dt, DateFormat format) {
        var value = (XMLGregorianCalendar) dt.clone();
        var o = new DateAndDateTimeChoice();
        switch (format) {
            case Date -> {
                value.setTime(0, 0, 0, 0);
                o.setDt(value);
                break;
            }
            case DateTime -> {
                o.setDtTm(value);
                break;
            }
            default -> throw new NotImplementedException(String.format("DateFormat %s unknown.", format));
        }
        return o;
    }

    private StructuredRemittanceInformation9 createStrd(StructuredReference[] structuredReferences, String invoiceNo) {
        StructuredRemittanceInformation9 strd = null;

        if (invoiceNo != null && invoiceNo.length() > 0) {
            strd = new StructuredRemittanceInformation9();
            var x = new ReferredDocumentInformation3();
            strd.getRfrdDocInf().add(x);
            x.setTp(new ReferredDocumentType2());
            x.getTp().setCdOrPrtry(new ReferredDocumentType1Choice());
            x.getTp().getCdOrPrtry().setCd(DocumentType5Code.CINV);
            x.setNb(invoiceNo);
        }

        if (structuredReferences != null && structuredReferences.length > 0) {
            if (strd == null) {
                strd = new StructuredRemittanceInformation9();
            }
            strd.setCdtrRefInf(new CreditorReferenceInformation2());
            for (var ref : structuredReferences) {
                strd.getCdtrRefInf().setTp(new CreditorReferenceType2());
                strd.getCdtrRefInf().getTp().setCdOrPrtry(new CreditorReferenceType1Choice());
                strd.getCdtrRefInf().getTp().getCdOrPrtry().setPrtry(CreditorReferenceConverter.toPrtry(ref.getType()));
                strd.getCdtrRefInf().setRef(ref.getUnformatted());
            }
        }

        return strd;
    }

    private BankTransactionCodeStructure5 createDomn() {
        var o = new BankTransactionCodeStructure5();
        o.setCd("PMNT");
        o.setFmly(new BankTransactionCodeStructure6());
        o.getFmly().setCd("RCDT");
        o.getFmly().setSubFmlyCd("VCOM");
        return o;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public DateFormat getBookingDateFormat() {
        return bookingDateFormat;
    }

    public void setBookingDateFormat(DateFormat bookingDateFormat) {
        this.bookingDateFormat = bookingDateFormat;
    }

    public DateFormat getValutaDateFormat() {
        return valutaDateFormat;
    }

    public void setValutaDateFormat(DateFormat valutaDateFormat) {
        this.valutaDateFormat = valutaDateFormat;
    }
}
