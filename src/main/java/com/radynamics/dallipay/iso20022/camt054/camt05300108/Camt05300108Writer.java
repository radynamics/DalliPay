package com.radynamics.dallipay.iso20022.camt054.camt05300108;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.WalletInfo;
import com.radynamics.dallipay.cryptoledger.WalletInfoAggregator;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.*;
import com.radynamics.dallipay.iso20022.camt054.*;
import com.radynamics.dallipay.iso20022.camt054.camt05300108.generated.*;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReference;
import com.radynamics.dallipay.transformation.TransformInstruction;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class Camt05300108Writer implements Camt054Writer {
    private final Ledger ledger;
    private final TransformInstruction transformInstruction;
    private final String productVersion;
    private IdGenerator idGenerator;
    private ZonedDateTime creationDate;

    public static final CamtFormat ExportFormat = CamtFormat.Camt05300108;
    private final LedgerCurrencyConverter ledgerCurrencyConverter;

    public Camt05300108Writer(Ledger ledger, TransformInstruction transformInstruction, String productVersion, LedgerCurrencyFormat ledgerCurrencyFormat) {
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

        d.setBkToCstmrStmt(new BankToCustomerStatementV08());
        d.getBkToCstmrStmt().setGrpHdr(new GroupHeader81());
        d.getBkToCstmrStmt().getGrpHdr().setMsgId(idGenerator.createMsgId());
        d.getBkToCstmrStmt().getGrpHdr().setCreDtTm(Utils.toXmlDateTime(creationDate));
        d.getBkToCstmrStmt().getGrpHdr().setAddtlInf(String.format("DalliPay/%s", productVersion));
        d.getBkToCstmrStmt().getGrpHdr().setMsgPgntn(new Pagination1());
        d.getBkToCstmrStmt().getGrpHdr().getMsgPgntn().setPgNb("1");
        d.getBkToCstmrStmt().getGrpHdr().getMsgPgntn().setLastPgInd(true);

        for (var t : transactions) {
            var receiverAccount = t.getReceiverAccount();
            var ccy = ledgerCurrencyConverter.getTargetCurrency(t.getUserCcy());
            var stmt = getNtfctnOrNull(d, receiverAccount, ccy.getCode());
            if (stmt == null) {
                stmt = new AccountStatement9();
                d.getBkToCstmrStmt().getStmt().add(stmt);
                stmt.setId(idGenerator.createStmId());
                stmt.setElctrncSeqNb(BigDecimal.valueOf(0));
                stmt.setCreDtTm(Utils.toXmlDateTime(creationDate));
                stmt.setAcct(createAcct(receiverAccount, ccy.getCode()));

                var opbd = reportBalances.getOpbd(ccy).orElse(null);
                if (opbd != null) {
                    stmt.getBal().add(createBalance("OPBD", opbd, reportBalances.getOpbdAt()));
                }
                var clbd = reportBalances.getClbd(ccy).orElse(null);
                if (clbd != null) {
                    stmt.getBal().add(createBalance("CLBD", clbd, reportBalances.getClbdAt()));
                }
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

    private AccountStatement9 getNtfctnOrNull(Document d, Account receiver, String ccy) {
        for (var ntfctn : d.getBkToCstmrStmt().getStmt()) {
            if (CashAccountCompare.isSame(ntfctn.getAcct(), createAcct(receiver, ccy))) {
                return ntfctn;
            }
        }
        return null;
    }

    private CashAccount39 createAcct(Account account, String ccy) {
        var acct = new CashAccount39();
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

    private CashBalance8 createBalance(String balanceType, Money amount, ZonedDateTime at) throws DatatypeConfigurationException {
        var bal = new CashBalance8();
        bal.setTp(new BalanceType13());
        var cd = new BalanceType10Choice();
        cd.setCd(balanceType);
        bal.getTp().setCdOrPrtry(cd);
        bal.setAmt(createAmt(amount));
        bal.setCdtDbtInd(CreditDebitCode.CRDT);
        bal.setDt(createDateAndDateTimeChoice(Utils.toXmlDateTime(at), transformInstruction.getBookingDateFormat()));
        return bal;
    }

    private ReportEntry10 createNtry(Payment trx, Account receiverAccount) throws DatatypeConfigurationException {
        var ntry = new ReportEntry10();

        // Seite 44: "Nicht standardisierte Verfahren: In anderen Fällen kann die «Referenz für den Kontoinhaber» geliefert werden."
        ntry.setNtryRef(receiverAccount.getUnformatted());

        var amt = createAmt(Money.of(trx.getAmount(), trx.getUserCcy()));

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

        var dtls = new EntryDetails9();
        ntry.getNtryDtls().add(dtls);
        dtls.setBtch(new BatchInformation2());
        dtls.getBtch().setNbOfTxs(String.valueOf(1));

        var txDtls = new EntryTransaction10();
        dtls.getTxDtls().add(txDtls);
        txDtls.setRefs(new TransactionReferences6());
        // Split due max allowed length of 35 per node (Max35Text)
        final int MAX_LEN = 35;
        var idPart0 = trx.getEndToEndId().substring(0, MAX_LEN);
        var idPart1 = trx.getEndToEndId().substring(MAX_LEN);
        txDtls.getRefs().setEndToEndId(idPart0);
        txDtls.getRefs().setMsgId(idPart1);

        txDtls.setAmt(amt);
        txDtls.setAmtDtls(amtDtls);
        txDtls.setCdtDbtInd(CreditDebitCode.CRDT);
        txDtls.setBkTxCd(new BankTransactionCodeStructure4());
        txDtls.getBkTxCd().setDomn(createDomn("AUTT"));

        txDtls.setRltdPties(createRltdPties(trx));

        txDtls.setRltdAgts(new TransactionAgents5());
        txDtls.getRltdAgts().setCdtrAgt(new BranchAndFinancialInstitutionIdentification6());
        txDtls.getRltdAgts().getCdtrAgt().setFinInstnId(new FinancialInstitutionIdentification18());
        txDtls.getRltdAgts().getCdtrAgt().getFinInstnId().setNm(ledger.getId().textId());

        var structuredReferences = WriterHelper.getStructuredReferences(transformInstruction, trx);
        var hasStrd = structuredReferences.length > 0 || trx.getInvoiceId() != null;
        if (hasStrd || trx.getMessages().length > 0) {
            txDtls.setRmtInf(new RemittanceInformation16());
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

    private ActiveOrHistoricCurrencyAndAmount createAmt(Money amount) {
        var m = ledgerCurrencyConverter.convert(amount);
        var amt = new ActiveOrHistoricCurrencyAndAmount();
        amt.setValue(AmountRounder.round(ledger, m, 2));
        amt.setCcy(m.getCcy().getCode());
        return amt;
    }

    private TransactionParties6 createRltdPties(Payment trx) {
        var obj = new TransactionParties6();

        var aggregator = new WalletInfoAggregator(trx.getLedger().getInfoProvider());
        obj.setDbtr(createPartyIdentification(aggregator.getNameOrDomain(trx.getSenderWallet())));
        obj.setCdtr(createPartyIdentification(aggregator.getNameOrDomain(trx.getReceiverWallet())));

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

    private StructuredRemittanceInformation16 createStrd(StructuredReference[] structuredReferences, String invoiceNo) {
        StructuredRemittanceInformation16 strd = null;

        if (invoiceNo != null && invoiceNo.length() > 0) {
            strd = new StructuredRemittanceInformation16();
            var x = new ReferredDocumentInformation7();
            strd.getRfrdDocInf().add(x);
            x.setTp(new ReferredDocumentType4());
            x.getTp().setCdOrPrtry(new ReferredDocumentType3Choice());
            x.getTp().getCdOrPrtry().setCd(DocumentType6Code.CINV);
            x.setNb(invoiceNo);
        }

        if (structuredReferences.length > 0) {
            if (strd == null) {
                strd = new StructuredRemittanceInformation16();
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
