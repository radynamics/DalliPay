package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400102.Camt05400102Writer;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400102.generated.Document;
import com.radynamics.CryptoIso20022Interop.transformation.TransactionTranslator;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import java.time.LocalDateTime;

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
        var payments = t.apply(TestFactory.createTransactions(cryptoInstruction.getLedger()));

        var w = new Camt05400102Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(LocalDateTime.of(2021, 06, 01, 16, 46, 10));
        var actual = camtConverter.toXml(w.createDocument(payments));
        var expected = camtConverter.toXml(camtConverter.toDocument(getClass().getClassLoader().getResourceAsStream(expectationResourceName)));

        assertThat(Input.fromByteArray(actual.toByteArray()), isSimilarTo(Input.fromByteArray(expected.toByteArray())));
    }
}
