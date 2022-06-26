package com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400109;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoAggregator;
import com.radynamics.CryptoIso20022Interop.iso20022.*;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.*;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400109.generated.*;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class Camt05400109Writer implements Camt054Writer {
    private final Ledger ledger;
    private TransformInstruction transformInstruction;
    private final String productVersion;
    private IdGenerator idGenerator;
    private ZonedDateTime creationDate;

    public static final CamtFormat ExportFormat = CamtFormat.Camt05400109;

    public Camt05400109Writer(Ledger ledger, TransformInstruction transformInstruction, String productVersion) {
        this.ledger = ledger;
        this.transformInstruction = transformInstruction;
        this.productVersion = productVersion;
        this.idGenerator = new UUIDIdGenerator();
        this.creationDate = ZonedDateTime.now();
    }

    @Override
    public Object createDocument(Payment[] transactions) throws Exception {
        var d = new Document();

        d.setBkToCstmrDbtCdtNtfctn(new BankToCustomerDebitCreditNotificationV09());
        d.getBkToCstmrDbtCdtNtfctn().setGrpHdr(new GroupHeader81());
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setMsgId(idGenerator.createMsgId());
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setCreDtTm(Utils.toXmlDateTime(creationDate));
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setAddtlInf(String.format("CryptoIso20022Interop/%s", productVersion));
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setMsgPgntn(new Pagination1());
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().getMsgPgntn().setPgNb("1");
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().getMsgPgntn().setLastPgInd(true);

        for (var t : transactions) {
            var receiver = t.getReceiverWallet();
            var stmt = getNtfctnOrNull(d, receiver);
            if (stmt == null) {
                stmt = new AccountNotification19();
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

    @Override
    public TransformInstruction getTransformInstruction() {
        return transformInstruction;
    }

    private AccountNotification19 getNtfctnOrNull(Document d, Wallet receiver) {
        for (var ntfctn : d.getBkToCstmrDbtCdtNtfctn().getNtfctn()) {
            if (CashAccountCompare.isSame(ntfctn.getAcct(), createAcct(receiver))) {
                return ntfctn;
            }
        }
        return null;
    }

    private CashAccount41 createAcct(Wallet receiver) {
        var acct = new CashAccount41();
        acct.setId(new AccountIdentification4Choice());
        var account = transformInstruction.getAccountOrNull(receiver);
        if (account == null) {
            acct.getId().setOthr(new GenericAccountIdentification1());
            acct.getId().getOthr().setId(receiver.getPublicKey());
        } else {
            if (account instanceof IbanAccount) {
                var iban = (IbanAccount) account;
                acct.getId().setIBAN(iban.getUnformatted());
            } else {
                acct.getId().setOthr(new GenericAccountIdentification1());
                acct.getId().getOthr().setId(account.getUnformatted());
            }
        }
        acct.setCcy(transformInstruction.getTargetCcy());

        return acct;
    }

    private ReportEntry11 createNtry(Payment trx) throws DatatypeConfigurationException {
        var ntry = new ReportEntry11();

        // Seite 44: "Nicht standardisierte Verfahren: In anderen Fällen kann die «Referenz für den Kontoinhaber» geliefert werden."
        ntry.setNtryRef(trx.getSenderAccount().getUnformatted());

        var amt = new ActiveOrHistoricCurrencyAndAmount();
        amt.setValue(AmountRounder.round(trx.getAmount(), 2));
        amt.setCcy(trx.getFiatCcy());

        ntry.setAmt(amt);
        var amtDtls = trx.createCcyPair().isOneToOne() ? null : createAmtDtls(trx);
        ntry.setAmtDtls(amtDtls);

        ntry.setCdtDbtInd(CreditDebitCode.CRDT);
        ntry.setSts(new EntryStatus1Choice());
        ntry.getSts().setCd("BOOK");

        var booked = Utils.toXmlDateTime(trx.getBooked());
        ntry.setBookgDt(createDateAndDateTimeChoice(booked, transformInstruction.getBookingDateFormat()));
        ntry.setValDt(createDateAndDateTimeChoice(booked, transformInstruction.getValutaDateFormat()));

        ntry.setBkTxCd(new BankTransactionCodeStructure4());
        ntry.getBkTxCd().setDomn(createDomn("VCOM"));

        var dtls = new EntryDetails10();
        ntry.getNtryDtls().add(dtls);
        dtls.setBtch(new BatchInformation2());
        dtls.getBtch().setNbOfTxs(String.valueOf(1));

        var txDtls = new EntryTransaction11();
        dtls.getTxDtls().add(txDtls);
        txDtls.setRefs(new TransactionReferences6());
        // Split due max allowed length of 35 per node (Max35Text)
        final int MAX_LEN = 35;
        var idPart0 = trx.getId().substring(0, MAX_LEN);
        var idPart1 = trx.getId().substring(MAX_LEN);
        txDtls.getRefs().setEndToEndId(idPart0);
        txDtls.getRefs().setMsgId(idPart1);

        txDtls.setAmt(amt);
        txDtls.setAmtDtls(amtDtls);
        txDtls.setCdtDbtInd(CreditDebitCode.CRDT);
        txDtls.setBkTxCd(new BankTransactionCodeStructure4());
        txDtls.getBkTxCd().setDomn(createDomn("AUTT"));

        txDtls.setRltdPties(createRltdPties(trx));

        txDtls.setRltdAgts(new TransactionAgents5());
        txDtls.getRltdAgts().setDbtrAgt(new BranchAndFinancialInstitutionIdentification6());
        txDtls.getRltdAgts().getDbtrAgt().setFinInstnId(new FinancialInstitutionIdentification18());
        txDtls.getRltdAgts().getDbtrAgt().getFinInstnId().setNm(ledger.getId().textId());

        var structuredReferences = WriterHelper.getStructuredReferences(transformInstruction, trx);
        var hasStrd = structuredReferences.length > 0 || trx.getInvoiceId() != null;
        if (hasStrd || trx.getMessages().length > 0) {
            txDtls.setRmtInf(new RemittanceInformation21());
        }

        if (hasStrd) {
            txDtls.getRmtInf().getStrd().add(createStrd(structuredReferences, trx.getInvoiceId()));
        }

        var sb = WriterHelper.getUstrd(trx);
        if (sb.length() > 0) {
            txDtls.getRmtInf().getUstrd().add(sb.toString());
        }

        return ntry;
    }

    private TransactionParties9 createRltdPties(Payment trx) {
        var obj = new TransactionParties9();

        var aggregator = new WalletInfoAggregator(trx.getLedger().getInfoProvider());
        obj.setDbtr(createPartyIdentification(aggregator.getMostImportant(trx.getSenderWallet())));
        obj.setCdtr(createPartyIdentification(aggregator.getMostImportant(trx.getReceiverWallet())));

        return obj.getDbtr() == null && obj.getCdtr() == null ? null : obj;
    }

    private Party40Choice createPartyIdentification(WalletInfo wi) {
        if (wi == null || StringUtils.isEmpty(wi.getValue())) {
            return null;
        }

        var obj = new Party40Choice();
        obj.setPty(new PartyIdentification135());
        obj.getPty().setNm(wi.getValue());
        return obj;
    }

    private AmountAndCurrencyExchange3 createAmtDtls(Payment trx) {
        var amtLedgerCcy = new ActiveOrHistoricCurrencyAndAmount();
        amtLedgerCcy.setValue(AmountRounder.round(trx.getAmountLedgerUnit(), 4));
        amtLedgerCcy.setCcy(trx.getLedgerCcy());

        var ccyXchg = new CurrencyExchange5();
        ccyXchg.setSrcCcy(trx.getLedgerCcy());
        ccyXchg.setTrgtCcy(trx.getFiatCcy());
        ccyXchg.setXchgRate(BigDecimal.valueOf(trx.getExchangeRate().getRate()));

        var o = new AmountAndCurrencyExchange3();
        o.setTxAmt(new AmountAndCurrencyExchangeDetails3());
        o.getTxAmt().setAmt(amtLedgerCcy);
        o.getTxAmt().setCcyXchg(ccyXchg);
        return o;
    }

    private DateAndDateTime2Choice createDateAndDateTimeChoice(XMLGregorianCalendar dt, DateFormat format) {
        var value = (XMLGregorianCalendar) dt.clone();
        var o = new DateAndDateTime2Choice();
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

    private StructuredRemittanceInformation17 createStrd(StructuredReference[] structuredReferences, String invoiceNo) {
        StructuredRemittanceInformation17 strd = null;

        if (invoiceNo != null && invoiceNo.length() > 0) {
            strd = new StructuredRemittanceInformation17();
            var x = new ReferredDocumentInformation7();
            strd.getRfrdDocInf().add(x);
            x.setTp(new ReferredDocumentType4());
            x.getTp().setCdOrPrtry(new ReferredDocumentType3Choice());
            x.getTp().getCdOrPrtry().setCd(DocumentType6Code.CINV);
            x.setNb(invoiceNo);
        }

        if (structuredReferences.length > 0) {
            if (strd == null) {
                strd = new StructuredRemittanceInformation17();
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

    private BankTransactionCodeStructure5 createDomn(String subFmlCd) {
        var o = new BankTransactionCodeStructure5();
        o.setCd("PMNT");
        o.setFmly(new BankTransactionCodeStructure6());
        o.getFmly().setCd("RCDT");
        o.getFmly().setSubFmlyCd(subFmlCd);
        return o;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public CamtFormat getExportFormat() {
        return ExportFormat;
    }
}
