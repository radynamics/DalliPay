package com.radynamics.dallipay.iso20022.camt054.camt05400102;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.*;
import com.radynamics.dallipay.iso20022.camt054.*;
import com.radynamics.dallipay.iso20022.camt054.camt05400102.generated.*;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReference;
import com.radynamics.dallipay.transformation.TransformInstruction;
import org.apache.commons.lang3.NotImplementedException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class Camt05400102Writer implements Camt054Writer {
    private final Ledger ledger;
    private final TransformInstruction transformInstruction;
    private final String productVersion;
    private IdGenerator idGenerator;
    private ZonedDateTime creationDate;

    public static final CamtFormat ExportFormat = CamtFormat.Camt05400102;
    private final LedgerCurrencyConverter ledgerCurrencyConverter;

    public Camt05400102Writer(Ledger ledger, TransformInstruction transformInstruction, String productVersion, LedgerCurrencyFormat ledgerCurrencyFormat) {
        this.ledger = ledger;
        this.transformInstruction = transformInstruction;
        this.productVersion = productVersion;
        this.idGenerator = new UUIDIdGenerator();
        this.creationDate = ZonedDateTime.now();
        this.ledgerCurrencyConverter = ledger.createLedgerCurrencyConverter(ledgerCurrencyFormat);
    }

    @Override
    public Object createDocument(Payment[] transactions, ReportBalances reportBalances) throws Exception {
        var d = new Document();

        d.setBkToCstmrDbtCdtNtfctn(new BankToCustomerDebitCreditNotificationV02());
        d.getBkToCstmrDbtCdtNtfctn().setGrpHdr(new GroupHeader42());
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setMsgId(idGenerator.createMsgId());
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setCreDtTm(Utils.toXmlDateTime(creationDate));
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setAddtlInf(String.format("DalliPay/%s", productVersion));
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().setMsgPgntn(new Pagination());
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().getMsgPgntn().setPgNb("1");
        d.getBkToCstmrDbtCdtNtfctn().getGrpHdr().getMsgPgntn().setLastPgInd(true);

        for (var t : transactions) {
            var receiverAccount = t.getReceiverAccount();
            var ccy = ledgerCurrencyConverter.getTargetCurrency(t.getUserCcy()).getCode();
            var stmt = getNtfctnOrNull(d, receiverAccount, ccy);
            if (stmt == null) {
                stmt = new AccountNotification2();
                d.getBkToCstmrDbtCdtNtfctn().getNtfctn().add(stmt);
                stmt.setId(idGenerator.createStmId());
                stmt.setElctrncSeqNb(BigDecimal.valueOf(0));
                stmt.setCreDtTm(Utils.toXmlDateTime(creationDate));
                stmt.setAcct(createAcct(receiverAccount, ccy));
            }

            stmt.getNtry().add(createNtry(t, receiverAccount));
            stmt.setElctrncSeqNb(stmt.getElctrncSeqNb().add(BigDecimal.ONE));
        }
        return d;
    }

    @Override
    public TransformInstruction getTransformInstruction() {
        return transformInstruction;
    }

    private AccountNotification2 getNtfctnOrNull(Document d, Account receiver, String ccy) {
        for (var ntfctn : d.getBkToCstmrDbtCdtNtfctn().getNtfctn()) {
            if (CashAccountCompare.isSame(ntfctn.getAcct(), createAcct(receiver, ccy))) {
                return ntfctn;
            }
        }
        return null;
    }

    private CashAccount20 createAcct(Account account, String ccy) {
        var acct = new CashAccount20();
        acct.setId(new AccountIdentification4Choice());
        if (account instanceof IbanAccount) {
            var iban = (IbanAccount) account;
            acct.getId().setIBAN(iban.getUnformatted());
        } else {
            acct.getId().setOthr(new GenericAccountIdentification1());
            acct.getId().getOthr().setId(account.getUnformatted());
        }
        acct.setCcy(ccy);

        return acct;
    }

    private ReportEntry2 createNtry(Payment trx, Account receiverAccount) throws DatatypeConfigurationException {
        var ntry = new ReportEntry2();

        // Seite 44: "Nicht standardisierte Verfahren: In anderen Fällen kann die «Referenz für den Kontoinhaber» geliefert werden."
        ntry.setNtryRef(receiverAccount.getUnformatted());

        var m = ledgerCurrencyConverter.convert(Money.of(trx.getAmount(), trx.getUserCcy()));
        var amt = new ActiveOrHistoricCurrencyAndAmount();
        amt.setValue(AmountRounder.round(ledger, m, 2));
        amt.setCcy(m.getCcy().getCode());

        ntry.setAmt(amt);
        var amtDtls = trx.createCcyPair().isOneToOne() ? null : createAmtDtls(trx);
        ntry.setAmtDtls(amtDtls);

        ntry.setCdtDbtInd(CreditDebitCode.CRDT);
        ntry.setSts(EntryStatus2Code.BOOK);

        var booked = Utils.toXmlDateTime(trx.getBooked());
        ntry.setBookgDt(createDateAndDateTimeChoice(booked, transformInstruction.getBookingDateFormat()));
        ntry.setValDt(createDateAndDateTimeChoice(booked, transformInstruction.getValutaDateFormat()));

        ntry.setBkTxCd(new BankTransactionCodeStructure4());
        ntry.getBkTxCd().setDomn(createDomn("VCOM"));

        var dtls = new EntryDetails1();
        ntry.getNtryDtls().add(dtls);
        dtls.setBtch(new BatchInformation2());
        dtls.getBtch().setNbOfTxs(String.valueOf(1));

        var txDtls = new EntryTransaction2();
        dtls.getTxDtls().add(txDtls);
        txDtls.setRefs(new TransactionReferences2());
        // Split due max allowed length of 35 per node (Max35Text)
        final int MAX_LEN = 35;
        var idPart0 = trx.getEndToEndId().substring(0, MAX_LEN);
        var idPart1 = trx.getEndToEndId().substring(MAX_LEN);
        txDtls.getRefs().setEndToEndId(idPart0);
        txDtls.getRefs().setMsgId(idPart1);

        txDtls.setAmtDtls(amtDtls);
        txDtls.setBkTxCd(new BankTransactionCodeStructure4());
        txDtls.getBkTxCd().setDomn(createDomn("AUTT"));

        txDtls.setRltdPties(createRltdPties(trx));

        txDtls.setRltdAgts(new TransactionAgents2());
        txDtls.getRltdAgts().setCdtrAgt(new BranchAndFinancialInstitutionIdentification4());
        txDtls.getRltdAgts().getCdtrAgt().setFinInstnId(new FinancialInstitutionIdentification7());
        txDtls.getRltdAgts().getCdtrAgt().getFinInstnId().setNm(ledger.getId().textId());

        var structuredReferences = WriterHelper.getStructuredReferences(transformInstruction, trx);
        var hasStrd = structuredReferences.length > 0 || trx.getInvoiceId() != null;
        if (hasStrd || trx.getMessages().length > 0) {
            txDtls.setRmtInf(new RemittanceInformation5());
        }

        if (hasStrd) {
            txDtls.getRmtInf().getStrd().add(createStrd(structuredReferences, trx.getInvoiceId()));
        }

        var sb = WriterHelper.getUstrd(trx);
        if (sb.length() > 0) {
            txDtls.getRmtInf().getUstrd().add(sb.length() <= 140 ? sb.toString() : sb.substring(0, 140));
        }

        return ntry;
    }

    private TransactionParty2 createRltdPties(Payment trx) {
        var obj = new TransactionParty2();

        var helper = new RelatedPartyHelper(trx.getLedger());
        // Null if sender could not get determined (eg. Bitcoin).
        var dbtr = helper.getNameOrDomain(trx.getSenderWallet()).orElse(trx.getSenderWallet() == null ? null : trx.getSenderWallet().getPublicKey());
        if (dbtr != null) {
            obj.setDbtr(createPartyIdentification(dbtr));
        }
        obj.setCdtr(createPartyIdentification(helper.getNameOrDomain(trx.getReceiverWallet()).orElse(null)));

        return obj.getDbtr() == null && obj.getCdtr() == null ? null : obj;
    }

    private PartyIdentification32 createPartyIdentification(String name) {
        if (name == null) {
            return null;
        }

        var obj = new PartyIdentification32();
        obj.setNm(name);
        return obj;
    }

    private AmountAndCurrencyExchange3 createAmtDtls(Payment trx) {
        var amtLedgerCcy = new ActiveOrHistoricCurrencyAndAmount();
        var m = ledgerCurrencyConverter.convert(trx.getAmountTransaction());
        amtLedgerCcy.setValue(AmountRounder.round(ledger, m, 4));
        amtLedgerCcy.setCcy(m.getCcy().getCode());

        var ccyXchg = new CurrencyExchange5();
        ccyXchg.setSrcCcy(m.getCcy().getCode());
        ccyXchg.setTrgtCcy(trx.getUserCcyCodeOrEmpty());
        ccyXchg.setXchgRate(ledgerCurrencyConverter.convert(trx.getExchangeRate()));

        var o = new AmountAndCurrencyExchange3();
        o.setTxAmt(new AmountAndCurrencyExchangeDetails3());
        o.getTxAmt().setAmt(amtLedgerCcy);
        o.getTxAmt().setCcyXchg(ccyXchg);
        return o;
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

    private StructuredRemittanceInformation7 createStrd(StructuredReference[] structuredReferences, String invoiceNo) {
        StructuredRemittanceInformation7 strd = null;

        if (invoiceNo != null && invoiceNo.length() > 0) {
            strd = new StructuredRemittanceInformation7();
            var x = new ReferredDocumentInformation3();
            strd.getRfrdDocInf().add(x);
            x.setTp(new ReferredDocumentType2());
            x.getTp().setCdOrPrtry(new ReferredDocumentType1Choice());
            x.getTp().getCdOrPrtry().setCd(DocumentType5Code.CINV);
            x.setNb(invoiceNo);
        }

        if (structuredReferences.length > 0) {
            if (strd == null) {
                strd = new StructuredRemittanceInformation7();
            }
            strd.setCdtrRefInf(new CreditorReferenceInformation2());
            for (var ref : structuredReferences) {
                var prtry = CreditorReferenceConverter.toPrtry(ref.getType());
                if (prtry != null) {
                    strd.getCdtrRefInf().setTp(new CreditorReferenceType2());
                    strd.getCdtrRefInf().getTp().setCdOrPrtry(new CreditorReferenceType1Choice());
                    strd.getCdtrRefInf().getTp().getCdOrPrtry().setPrtry(prtry);
                }
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

    @Override
    public LedgerCurrencyFormat getExportLedgerCurrencyFormat() {
        return ledgerCurrencyConverter.getTargetFormat();
    }
}
