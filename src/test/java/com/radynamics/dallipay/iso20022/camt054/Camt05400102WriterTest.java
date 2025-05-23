package com.radynamics.dallipay.iso20022.camt054;

import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.iso20022.TestWalletInfoProvider;
import com.radynamics.dallipay.iso20022.camt054.camt05400102.Camt05400102Writer;
import com.radynamics.dallipay.iso20022.camt054.camt05400102.generated.Document;
import com.radynamics.dallipay.iso20022.pain001.TestLedger;
import com.radynamics.dallipay.transformation.TransactionTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class Camt05400102WriterTest {
    private static final CamtConverter camtConverter = new CamtConverter(Document.class);
    private static final String ProductVersion = "0.1.2-SNAPSHOT";

    @Test
    public void testLedgerCcy() throws Exception {
        test("XRP", "camt054/camt.054.001.02_testCreate2Payments.xml");
    }

    @Test
    public void testUsd() throws Exception {
        test("USD", "camt054/camt.054.001.02_testCreate2Payments.USD.xml");
    }

    private void test(String targetCcy, String expectationResourceName) throws Exception {
        var cryptoInstruction = TestFactory.createTransformInstruction();
        cryptoInstruction.setTargetCcy(targetCcy);

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(TestFactory.createTransactions(cryptoInstruction.getLedger(), targetCcy));

        var w = new Camt05400102Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion, LedgerCurrencyFormat.Native);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(TestFactory.createCreationDate());
        var actual = camtConverter.toXml(w.createDocument(payments, ReportBalances.Empty));
        var expected = camtConverter.toXml(camtConverter.toDocument(getClass().getClassLoader().getResourceAsStream(expectationResourceName)));

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
        var w = new Camt05400102Writer(ti.getLedger(), ti, ProductVersion, LedgerCurrencyFormat.Native);
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
        var w = new Camt05400102Writer(ti.getLedger(), ti, ProductVersion, LedgerCurrencyFormat.Native);
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
        var w = new Camt05400102Writer(ti.getLedger(), ti, ProductVersion, LedgerCurrencyFormat.Native);
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
