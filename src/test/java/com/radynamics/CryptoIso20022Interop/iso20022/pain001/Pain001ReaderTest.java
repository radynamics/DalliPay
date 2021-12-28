package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.transformation.AccountMapping;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class Pain001ReaderTest {
    @Test
    public void readPain001ExampleZA1() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger);
        // DbtrAcct
        ti.add(new AccountMapping("CH5481230000001998736", "sender_CH5481230000001998736"));
        // CdtrAcct
        // TODO: 2021-12-28 create specific types (new OtherAccount("010832052"), new IbanAccount(...))
        ti.add(new AccountMapping("010832052", "receiver_010832052"));
        ti.add(new AccountMapping("010391391", "receiver_010391391"));
        ti.add(new AccountMapping("010649858", "receiver_010649858"));
        ti.add(new AccountMapping("032233441", "receiver_032233441"));
        ExchangeRate[] rates = {
                new ExchangeRate("CHF", ledger.getNativeCcySymbol(), 1),
                new ExchangeRate("EUR", ledger.getNativeCcySymbol(), 1),
        };
        var ccyConverter = new CurrencyConverter(rates);
        var r = new Pain001Reader(ledger, ti, ccyConverter);

        var transactions = r.read(getClass().getClassLoader().getResourceAsStream("pain001/Six/pain001ExampleZA1.xml"));

        assertNotNull(transactions);
        assertEquals(4, transactions.length);

        assertTransaction(transactions[0], "receiver_010832052", 459000, "000000000000060029920346303");
        assertTransaction(transactions[1], "receiver_010391391", 3949750, "210000000003139471430009017");
        assertTransaction(transactions[2], "receiver_010649858", 2838640, "030015972590806420080020801");
        assertTransaction(transactions[3], "receiver_032233441", 1727530, "332015900002760103813712236");
    }

    private void assertTransaction(Transaction t, String receiver, double amount, String referenceUnformatted) {
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
        assertEquals(1, t.getStructuredReferences().length);
        assertEquals(ReferenceType.Isr, t.getStructuredReferences()[0].getType());
        assertEquals(referenceUnformatted, t.getStructuredReferences()[0].getUnformatted());
    }
}
