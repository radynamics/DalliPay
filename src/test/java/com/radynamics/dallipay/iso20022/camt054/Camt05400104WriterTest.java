package com.radynamics.dallipay.iso20022.camt054;

import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.iso20022.IbanAccount;
import com.radynamics.dallipay.iso20022.PaymentConverter;
import com.radynamics.dallipay.iso20022.TestWalletInfoProvider;
import com.radynamics.dallipay.iso20022.camt054.camt05400104.Camt05400104Writer;
import com.radynamics.dallipay.iso20022.camt054.camt05400104.generated.Document;
import com.radynamics.dallipay.iso20022.creditorreference.ReferenceType;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReferenceFactory;
import com.radynamics.dallipay.iso20022.pain001.TestLedger;
import com.radynamics.dallipay.transformation.TransactionTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.xmlunit.builder.Input;

import static org.junit.Assert.*;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class Camt05400104WriterTest {
    private static final CamtConverter camtConverter = new CamtConverter(Document.class);
    private static final String ProductVersion = "0.1.2-SNAPSHOT";

    @Test
    public void testCreate2PaymentsLedgerCcy() throws Exception {
        testCreate2Payments("XRP", "camt054/camt.054.001.04_testCreate2Payments.xml");
    }

    @Test
    public void testCreate2PaymentsUsd() throws Exception {
        testCreate2Payments("USD", "camt054/camt.054.001.04_testCreate2Payments.USD.xml");
    }

    private void testCreate2Payments(String targetCcy, String expectationResourceName) throws Exception {
        var cryptoInstruction = TestFactory.createTransformInstruction();
        cryptoInstruction.setTargetCcy(targetCcy);

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(TestFactory.createTransactions(cryptoInstruction.getLedger(), targetCcy));

        var w = new Camt05400104Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion, LedgerCurrencyFormat.Native);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(TestFactory.createCreationDate());
        var actual = camtConverter.toXml(w.createDocument(payments, ReportBalances.Empty));
        var expected = camtConverter.toXml(camtConverter.toDocument(getClass().getClassLoader().getResourceAsStream(expectationResourceName)));

        assertThat(Input.fromByteArray(actual.toByteArray()), isSimilarTo(Input.fromByteArray(expected.toByteArray())));
    }

    @Test
    public void createTranslateToIban() throws Exception {
        var cryptoInstruction = TestFactory.createTransformInstruction();
        TestFactory.addAccountMapping(cryptoInstruction, new IbanAccount("CH5800791123000889012"), "rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY");

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(TestFactory.createTransactions(cryptoInstruction.getLedger(), "XRP"));
        var w = new Camt05400104Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion, LedgerCurrencyFormat.Native);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(TestFactory.createCreationDate());
        var actual = (Document) w.createDocument(payments, ReportBalances.Empty);

        var acct = actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(0).getAcct();
        assertNotNull(acct);
        assertNotNull(acct.getId());
        assertEquals("CH5800791123000889012", acct.getId().getIBAN());
    }

    @ParameterizedTest
    @EnumSource(DateFormat.class)
    public void createBookedFormat(DateFormat format) throws Exception {
        var cryptoInstruction = TestFactory.createTransformInstruction();
        cryptoInstruction.setBookingDateFormat(format);
        cryptoInstruction.setValutaDateFormat(format);

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(TestFactory.createTransactions(cryptoInstruction.getLedger(), "XRP"));
        var w = new Camt05400104Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion, LedgerCurrencyFormat.Native);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(TestFactory.createCreationDate());
        var actual = (Document) w.createDocument(payments, ReportBalances.Empty);

        var ntry = actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(0).getNtry().get(0);
        assertNotNull(ntry);
        assertNotNull(ntry.getBookgDt());
        assertNotNull(ntry.getValDt());
        switch (format) {
            case Date -> {
                assertEquals("2021-02-21T00:00:00.000+01:00", ntry.getBookgDt().getDt().toString());
                assertEquals("2021-02-21T00:00:00.000+01:00", ntry.getValDt().getDt().toString());
            }
            case DateTime -> {
                assertEquals("2021-02-21T09:10:11.000+01:00", ntry.getBookgDt().getDtTm().toString());
                assertEquals("2021-02-21T09:10:11.000+01:00", ntry.getValDt().getDtTm().toString());
            }
            default -> throw new IllegalStateException("Unexpected value: " + format);
        }
    }

    @Test
    public void createCreditorReferenceIfMissing() throws Exception {
        var cryptoInstruction = TestFactory.createTransformInstruction();
        cryptoInstruction.setCreditorReferenceIfMissing(StructuredReferenceFactory.create(ReferenceType.Scor, "RF040000000000"));

        var trx = TestFactory.createTransactionScor(cryptoInstruction.getLedger());
        trx.removeStructuredReferences(0);

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(PaymentConverter.toPayment(new Transaction[]{trx}, new Currency("XRP")));

        var w = new Camt05400104Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion, LedgerCurrencyFormat.Native);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(TestFactory.createCreationDate());
        var actual = camtConverter.toXml(w.createDocument(payments, ReportBalances.Empty));
        var expected = camtConverter.toXml(camtConverter.toDocument(getClass().getClassLoader().getResourceAsStream("camt054/camt.054.001.04_createCreditorReferenceIfMissing.xml")));

        assertThat(Input.fromByteArray(actual.toByteArray()), isSimilarTo(Input.fromByteArray(expected.toByteArray())));
    }

    @Test
    public void createRelatedPartiesNm() throws Exception {
        var ledger = new TestLedger();
        var ti = TestFactory.createTransformInstruction(ledger);

        var wip = new TestWalletInfoProvider();
        wip.addName("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", "Company A");
        wip.addName("rhEo7YkHrxMzqwPhCASpeNwL2HNMqfsb87", "Company B");
        ledger.setInfoProvider(new WalletInfoProvider[]{wip});

        var t = new TransactionTranslator(ti, new CurrencyConverter(new ExchangeRate[0]));
        var payments = t.apply(TestFactory.createTransactions(ti.getLedger(), "TEST"));
        var w = new Camt05400104Writer(ti.getLedger(), ti, ProductVersion, LedgerCurrencyFormat.Native);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(TestFactory.createCreationDate());

        var actual = (Document) w.createDocument(payments, ReportBalances.Empty);

        var rltdPties = actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(0).getNtry().get(0).getNtryDtls().get(0).getTxDtls().get(0).getRltdPties();

        Assertions.assertNotNull(rltdPties.getDbtr());
        Assertions.assertEquals("Company B", rltdPties.getDbtr().getNm());

        Assertions.assertNotNull(rltdPties.getCdtr());
        Assertions.assertEquals("Company A", rltdPties.getCdtr().getNm());
    }

    @Test
    public void multipleCurrencies() throws Exception {
        var ledger = new TestLedger();
        var ti = TestFactory.createTransformInstruction(ledger);
        var w = new Camt05400104Writer(ti.getLedger(), ti, ProductVersion, LedgerCurrencyFormat.Native);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(TestFactory.createCreationDate());

        var actual = (Document) w.createDocument(TestFactory.createTransactionsMultiCcy(ledger, ti), ReportBalances.Empty);

        Assertions.assertEquals(2, actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().size());
        {
            var ntfctn = actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(0);
            Assertions.assertEquals(1, ntfctn.getNtry().size());
            Assertions.assertEquals("TEST", ntfctn.getAcct().getCcy());
            Assertions.assertEquals(36.35, ntfctn.getNtry().get(0).getAmt().getValue().doubleValue());
            Assertions.assertEquals("TEST", ntfctn.getNtry().get(0).getAmt().getCcy());
        }
        {
            var ntfctn = actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(1);
            Assertions.assertEquals(1, ntfctn.getNtry().size());
            Assertions.assertEquals("XYZ", ntfctn.getAcct().getCcy());
            Assertions.assertEquals(7777.77, ntfctn.getNtry().get(0).getAmt().getValue().doubleValue());
            Assertions.assertEquals("XYZ", ntfctn.getNtry().get(0).getAmt().getCcy());
        }
    }

    @Test
    public void RmtInfUstrdLength() throws Exception {
        var ledger = new TestLedger();
        var ti = TestFactory.createTransformInstruction(ledger);
        var w = new Camt05400104Writer(ti.getLedger(), ti, ProductVersion, LedgerCurrencyFormat.Native);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(TestFactory.createCreationDate());

        var actual = (Document) w.createDocument(TestFactory.createTransactionsMaxRmtInfUstrd(ledger, ti), ReportBalances.Empty);

        Assertions.assertEquals(2, actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(0).getNtry().size());
        {
            var ustrd = actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(0).getNtry().get(0).getNtryDtls().get(0).getTxDtls().get(0).getRmtInf().getUstrd();
            Assertions.assertEquals(1, ustrd.size());
            Assertions.assertEquals("0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_01234567", ustrd.get(0));
        }
        {
            var ustrd = actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(0).getNtry().get(1).getNtryDtls().get(0).getTxDtls().get(0).getRmtInf().getUstrd();
            Assertions.assertEquals(1, ustrd.size());
            Assertions.assertEquals("0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789 0123456789_0123456789_0123456789_0123456789_0123456789_01234567", ustrd.get(0));
        }
    }
}