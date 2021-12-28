package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.OtherAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.transformation.AccountMapping;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class Pain001ReaderTest {
    @Test
    public void readExampleZA1() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger);
        // DbtrAcct
        ti.add(new AccountMapping(new IbanAccount("CH5481230000001998736"), "sender_CH5481230000001998736"));
        // CdtrAcct
        ti.add(new AccountMapping(new OtherAccount("010832052"), "receiver_010832052"));
        ti.add(new AccountMapping(new OtherAccount("010391391"), "receiver_010391391"));
        ti.add(new AccountMapping(new OtherAccount("010649858"), "receiver_010649858"));
        ti.add(new AccountMapping(new OtherAccount("032233441"), "receiver_032233441"));
        ExchangeRate[] rates = {
                new ExchangeRate("CHF", ledger.getNativeCcySymbol(), 1),
                new ExchangeRate("EUR", ledger.getNativeCcySymbol(), 1),
        };
        var ccyConverter = new CurrencyConverter(rates);
        var r = new Pain001Reader(ledger, ti, ccyConverter);

        var transactions = r.read(getClass().getClassLoader().getResourceAsStream("pain001/Six/pain001ExampleZA1.xml"));

        assertNotNull(transactions);
        assertEquals(4, transactions.length);

        assertTransaction(transactions[0], "receiver_010832052", 459000, ReferenceType.Isr, "000000000000060029920346303");
        assertTransaction(transactions[1], "receiver_010391391", 3949750, ReferenceType.Isr, "210000000003139471430009017");
        assertTransaction(transactions[2], "receiver_010649858", 2838640, ReferenceType.Isr, "030015972590806420080020801");
        assertTransaction(transactions[3], "receiver_032233441", 1727530, ReferenceType.Isr, "332015900002760103813712236");
    }

    @Test
    public void readExampleZA6Scor() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger);
        // DbtrAcct
        ti.add(new AccountMapping(new IbanAccount("CH5481230000001998736"), "sender_CH5481230000001998736"));
        // CdtrAcct
        ti.add(new AccountMapping(new IbanAccount("GB96MIDL40271522859882"), "receiver_GB96MIDL40271522859882"));
        ti.add(new AccountMapping(new OtherAccount("40271522859882"), "receiver_40271522859882"));
        ti.add(new AccountMapping(new IbanAccount("GB96MIDL40271522859882"), "receiver_GB96MIDL40271522859882"));
        ti.add(new AccountMapping(new IbanAccount("GB96MIDL40271522859882"), "receiver_GB96MIDL40271522859882"));
        ExchangeRate[] rates = {
                new ExchangeRate("CHF", ledger.getNativeCcySymbol(), 1),
                new ExchangeRate("GBP", ledger.getNativeCcySymbol(), 1),
        };
        var ccyConverter = new CurrencyConverter(rates);
        var r = new Pain001Reader(ledger, ti, ccyConverter);

        var transactions = r.read(getClass().getClassLoader().getResourceAsStream("pain001/Six/pain001ExampleZA6Scor.xml"));

        assertNotNull(transactions);
        assertEquals(4, transactions.length);

        assertTransaction(transactions[0], "receiver_GB96MIDL40271522859882", 5000000, ReferenceType.Scor, "RF712348231");
        assertTransaction(transactions[1], "receiver_40271522859882", 6000000);
        assertTransaction(transactions[2], "receiver_GB96MIDL40271522859882", 7000000);
        assertTransaction(transactions[3], "receiver_GB96MIDL40271522859882", 8000000);
    }

    @Test
    public void readSwissQrBillWithQrReference() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger);
        // DbtrAcct
        ti.add(new AccountMapping(new IbanAccount("CH5481230000001998736"), "sender_CH5481230000001998736"));
        // CdtrAcct
        ti.add(new AccountMapping(new IbanAccount("CH4431999123000889012"), "receiver_CH4431999123000889012"));
        ExchangeRate[] rates = {
                new ExchangeRate("CHF", ledger.getNativeCcySymbol(), 1),
        };
        var ccyConverter = new CurrencyConverter(rates);
        var r = new Pain001Reader(ledger, ti, ccyConverter);

        var transactions = r.read(getClass().getClassLoader().getResourceAsStream("pain001/Six/pain001SwissQrBillWithQrReference.xml"));

        assertNotNull(transactions);
        assertEquals(1, transactions.length);

        assertTransaction(transactions[0], "receiver_CH4431999123000889012", 1949750, ReferenceType.SwissQrBill, "210000000003139471430009017");
    }

    @Test
    public void readSwissQrBillWithScorReference() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger);
        // DbtrAcct
        ti.add(new AccountMapping(new IbanAccount("CH5481230000001998736"), "sender_CH5481230000001998736"));
        // CdtrAcct
        ti.add(new AccountMapping(new IbanAccount("CH5800791123000889012"), "receiver_CH4431999123000889012"));
        ExchangeRate[] rates = {
                new ExchangeRate("CHF", ledger.getNativeCcySymbol(), 1),
        };
        var ccyConverter = new CurrencyConverter(rates);
        var r = new Pain001Reader(ledger, ti, ccyConverter);

        var transactions = r.read(getClass().getClassLoader().getResourceAsStream("pain001/Six/pain001SwissQrBillWithScorReference.xml"));

        assertNotNull(transactions);
        assertEquals(1, transactions.length);

        assertTransaction(transactions[0], "receiver_CH4431999123000889012", 199950, ReferenceType.Scor, "RF18539007547034");
    }

    private void assertTransaction(Transaction t, String receiver, double amount) {
        assertTransaction(t, receiver, amount, null, null);
    }

    private void assertTransaction(Transaction t, String receiver, double amount, ReferenceType type, String referenceUnformatted) {
        assertNotNull(t.getSender());
        assertEquals("sender_CH5481230000001998736", t.getSender().getPublicKey());
        assertEquals(amount, t.getAmountSmallestUnit(), 0);
        assertEquals("TEST", t.getCcy());
        assertNotNull(t.getReceiver());
        assertEquals(receiver, t.getReceiver().getPublicKey());
        assertNull(t.getId());
        assertNull(t.getInvoiceId());
        assertNotNull(t.getMessages());
        assertEquals(0, t.getMessages().length);
        assertNotNull(t.getStructuredReferences());
        if (referenceUnformatted == null) {
            assertEquals(0, t.getStructuredReferences().length);
        } else {
            assertEquals(1, t.getStructuredReferences().length);
            assertEquals(type, t.getStructuredReferences()[0].getType());
            assertEquals(referenceUnformatted, t.getStructuredReferences()[0].getUnformatted());
        }
    }
}
