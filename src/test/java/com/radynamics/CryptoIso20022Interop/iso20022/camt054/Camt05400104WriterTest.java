package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.PaymentConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.Camt05400104Writer;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.generated.Document;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import com.radynamics.CryptoIso20022Interop.transformation.AccountMapping;
import com.radynamics.CryptoIso20022Interop.transformation.TransactionTranslator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.xmlunit.builder.Input;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class Camt05400104WriterTest {
    private static final CamtConverter camtConverter = new CamtConverter(Document.class);
    private static final String ProductVersion = "0.1.2-SNAPSHOT";

    @Test
    public void testCreate2Payments() throws Exception {
        var cryptoInstruction = TestFactory.createTransformInstruction();

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(TestFactory.createTransactions(cryptoInstruction.getLedger()));

        var w = new Camt05400104Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(LocalDateTime.of(2021, 06, 01, 16, 46, 10));
        var actual = camtConverter.toXml(w.createDocument(payments));
        var expected = camtConverter.toXml(camtConverter.toDocument(getClass().getClassLoader().getResourceAsStream("camt054/testCreate2Payments.xml")));

        assertThat(Input.fromByteArray(actual.toByteArray()), isSimilarTo(Input.fromByteArray(expected.toByteArray())));
    }

    @Test
    public void createTranslateToIban() throws Exception {
        var cryptoInstruction = TestFactory.createTransformInstruction();
        cryptoInstruction.add(new AccountMapping(new IbanAccount("CH5800791123000889012"), "rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"));

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(TestFactory.createTransactions(cryptoInstruction.getLedger()));
        var w = new Camt05400104Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(LocalDateTime.of(2021, 06, 01, 16, 46, 10));
        var actual = (Document) w.createDocument(payments);

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
        var payments = t.apply(TestFactory.createTransactions(cryptoInstruction.getLedger()));
        var w = new Camt05400104Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(LocalDateTime.of(2021, 06, 01, 16, 46, 10));
        var actual = (Document) w.createDocument(payments);

        var ntry = actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(0).getNtry().get(0);
        assertNotNull(ntry);
        assertNotNull(ntry.getBookgDt());
        assertNotNull(ntry.getValDt());
        switch (format) {
            case Date -> {
                assertEquals("2021-02-21T00:00:00.000Z", ntry.getBookgDt().getDt().toString());
                assertEquals("2021-02-21T00:00:00.000Z", ntry.getValDt().getDt().toString());
            }
            case DateTime -> {
                assertEquals("2021-02-21T09:10:11.000Z", ntry.getBookgDt().getDtTm().toString());
                assertEquals("2021-02-21T09:10:11.000Z", ntry.getValDt().getDtTm().toString());
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
        var payments = t.apply(PaymentConverter.toPayment(new Transaction[]{trx}));

        var w = new Camt05400104Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(LocalDateTime.of(2021, 06, 01, 16, 46, 10));
        var actual = camtConverter.toXml(w.createDocument(payments));
        var expected = camtConverter.toXml(camtConverter.toDocument(getClass().getClassLoader().getResourceAsStream("camt054/createCreditorReferenceIfMissing.xml")));

        assertThat(Input.fromByteArray(actual.toByteArray()), isSimilarTo(Input.fromByteArray(expected.toByteArray())));
    }
}