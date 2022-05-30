package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoProvider;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.TestWalletInfoProvider;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400109.Camt05400109Writer;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400109.generated.Document;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestLedger;
import com.radynamics.CryptoIso20022Interop.transformation.TransactionTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class Camt05400109WriterTest {
    private static final CamtConverter camtConverter = new CamtConverter(Document.class);
    private static final String ProductVersion = "0.1.2-SNAPSHOT";

    @Test
    public void testCreate2PaymentsLedgerCcy() throws Exception {
        testCreate2Payments("XRP", "camt054/camt.054.001.09_testCreate2Payments.xml");
    }

    @Test
    public void testCreate2PaymentsUsd() throws Exception {
        testCreate2Payments("USD", "camt054/camt.054.001.09_testCreate2Payments.USD.xml");
    }

    private void testCreate2Payments(String targetCcy, String expectationResourceName) throws Exception {
        var cryptoInstruction = TestFactory.createTransformInstruction();
        cryptoInstruction.setTargetCcy(targetCcy);

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(TestFactory.createTransactions(cryptoInstruction.getLedger(), targetCcy));

        var w = new Camt05400109Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(TestFactory.createCreationDate());
        var actual = camtConverter.toXml(w.createDocument(payments));
        var expected = camtConverter.toXml(camtConverter.toDocument(getClass().getClassLoader().getResourceAsStream(expectationResourceName)));

        assertThat(Input.fromByteArray(actual.toByteArray()), isSimilarTo(Input.fromByteArray(expected.toByteArray())));
    }


    @Test
    public void createRelatedPartiesNm() throws Exception {
        var ledger = new TestLedger();
        var ti = TestFactory.createTransformInstruction(ledger);

        var wip = new TestWalletInfoProvider();
        wip.add("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", new WalletInfo("Name", "Company A", 1));
        wip.add("rhEo7YkHrxMzqwPhCASpeNwL2HNMqfsb87", new WalletInfo("Name", "Company B", 1));
        ledger.setInfoProvider(new WalletInfoProvider[]{wip});

        var t = new TransactionTranslator(ti, new CurrencyConverter(new ExchangeRate[0]));
        var payments = t.apply(TestFactory.createTransactions(ti.getLedger(), "TEST"));
        var w = new Camt05400109Writer(ti.getLedger(), ti, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(TestFactory.createCreationDate());

        var actual = (Document) w.createDocument(payments);

        var rltdPties = actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(0).getNtry().get(0).getNtryDtls().get(0).getTxDtls().get(0).getRltdPties();

        Assertions.assertNotNull(rltdPties.getDbtr());
        Assertions.assertNotNull(rltdPties.getDbtr().getPty());
        Assertions.assertEquals("Company B", rltdPties.getDbtr().getPty().getNm());

        Assertions.assertNotNull(rltdPties.getCdtr());
        Assertions.assertNotNull(rltdPties.getCdtr().getPty());
        Assertions.assertEquals("Company A", rltdPties.getCdtr().getPty().getNm());
    }
}