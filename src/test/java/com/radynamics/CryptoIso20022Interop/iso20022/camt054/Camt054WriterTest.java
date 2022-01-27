package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.DemoExchange;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.PaymentConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import com.radynamics.CryptoIso20022Interop.transformation.AccountMapping;
import com.radynamics.CryptoIso20022Interop.transformation.TransactionTranslator;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.xmlunit.builder.Input;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class Camt054WriterTest {
    private static final String ProductVersion = "0.1.2-SNAPSHOT";

    @Test
    public void testCreate2Payments() throws Exception {
        var cryptoInstruction = createTestInstructions();

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(createTestTransactions(cryptoInstruction.getLedger()));

        var w = new Camt054Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(LocalDateTime.of(2021, 06, 01, 16, 46, 10));
        var actual = CamtConverter.toXml(w.create(payments));
        var expected = CamtConverter.toXml(CamtConverter.toDocument(getClass().getClassLoader().getResourceAsStream("camt054/testCreate2Payments.xml")));

        assertThat(Input.fromByteArray(actual.toByteArray()), isSimilarTo(Input.fromByteArray(expected.toByteArray())));
    }

    private static TransformInstruction createTestInstructions() {
        var ledger = LedgerFactory.create("xrpl");
        ledger.setNetwork(new NetworkInfo(Network.Test, null));
        var i = new TransformInstruction(ledger);
        i.setExchangeRateProvider(new DemoExchange());
        i.setTargetCcy(ledger.getNativeCcySymbol());

        return i;
    }

    private static Payment[] createTestTransactions(Ledger ledger) {
        var list = new ArrayList<Transaction>();
        list.add(createTestTransaction1(ledger));
        list.add(createTestTransaction2(ledger));
        list.add(createTestTransactionScor(ledger));
        return PaymentConverter.toPayment(list.toArray(new Transaction[0]));
    }

    private static Transaction createTestTransaction1(Ledger ledger) {
        var t = ledger.createTransaction();
        t.setSenderWallet(ledger.createWallet("rhEo7YkHrxMzqwPhCASpeNwL2HNMqfsb87", null));
        t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
        t.setAmountSmallestUnit(36350000);
        t.setId("E43D83F7869885BFE92C29A6A7CF48F9B9B2FE1CEB95384707584A9DB3E288EA");
        t.setBooked(LocalDateTime.of(2021, 02, 21, 9, 10, 11));
        t.setInvoiceId("RG-00123.45");

        return t;
    }

    private static Transaction createTestTransaction2(Ledger ledger) {
        var t = ledger.createTransaction();
        t.setSenderWallet(ledger.createWallet("rsDoF5udkeSJQcKNqPgHvqEyVBEX4ttoi4", null));
        t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
        t.setAmountSmallestUnit(50000000);
        t.setId("57237F065509B36FB3B31DA771B6AFBBF943E3D3E9D64A3548A6C52BD7CE9415");
        t.setBooked(LocalDateTime.of(2021, 02, 21, 9, 10, 11));
        t.addStructuredReference(StructuredReferenceFactory.create(ReferenceType.SwissQrBill, "210000000003139471430009017"));

        return t;
    }

    private static Transaction createTestTransactionScor(Ledger ledger) {
        var t = ledger.createTransaction();
        t.setSenderWallet(ledger.createWallet("rsDoF5udkeSJQcKNqPgHvqEyVBEX4ttoi4", null));
        t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
        t.setAmountSmallestUnit(391000000);
        t.setId("4CA4105CBC1288D9C3FB5140C61097B247523AB86192C87B89121F4877351DD9");
        t.setBooked(LocalDateTime.of(2021, 12, 28, 11, 15, 11));
        t.addStructuredReference(StructuredReferenceFactory.create(ReferenceType.Scor, "RF712348231"));

        return t;
    }

    @Test
    public void createTranslateToIban() throws Exception {
        var cryptoInstruction = createTestInstructions();
        cryptoInstruction.add(new AccountMapping(new IbanAccount("CH5800791123000889012"), "rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"));

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(createTestTransactions(cryptoInstruction.getLedger()));
        var w = new Camt054Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(LocalDateTime.of(2021, 06, 01, 16, 46, 10));
        var actual = w.create(payments);

        var acct = actual.getBkToCstmrDbtCdtNtfctn().getNtfctn().get(0).getAcct();
        assertNotNull(acct);
        assertNotNull(acct.getId());
        assertEquals("CH5800791123000889012", acct.getId().getIBAN());
    }

    @ParameterizedTest
    @EnumSource(DateFormat.class)
    public void createBookedFormat(DateFormat format) throws Exception {
        var cryptoInstruction = createTestInstructions();
        cryptoInstruction.setBookingDateFormat(format);
        cryptoInstruction.setValutaDateFormat(format);

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(createTestTransactions(cryptoInstruction.getLedger()));
        var w = new Camt054Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(LocalDateTime.of(2021, 06, 01, 16, 46, 10));
        var actual = w.create(payments);

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
        var cryptoInstruction = createTestInstructions();
        cryptoInstruction.setCreditorReferenceIfMissing(StructuredReferenceFactory.create(ReferenceType.Scor, "RF040000000000"));

        var trx = createTestTransactionScor(cryptoInstruction.getLedger());
        trx.removeStructuredReferences(0);

        var t = new TransactionTranslator(cryptoInstruction, new CurrencyConverter(cryptoInstruction.getExchangeRateProvider().latestRates()));
        var payments = t.apply(PaymentConverter.toPayment(new Transaction[]{trx}));

        var w = new Camt054Writer(cryptoInstruction.getLedger(), cryptoInstruction, ProductVersion);
        w.setIdGenerator(new FixedValueIdGenerator());
        w.setCreationDate(LocalDateTime.of(2021, 06, 01, 16, 46, 10));
        var actual = CamtConverter.toXml(w.create(payments));
        var expected = CamtConverter.toXml(CamtConverter.toDocument(getClass().getClassLoader().getResourceAsStream("camt054/createCreditorReferenceIfMissing.xml")));

        assertThat(Input.fromByteArray(actual.toByteArray()), isSimilarTo(Input.fromByteArray(expected.toByteArray())));
    }
}