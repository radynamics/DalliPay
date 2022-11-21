package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.Config;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.Address;
import com.radynamics.CryptoIso20022Interop.transformation.MemoryAccountMappingSource;
import com.radynamics.CryptoIso20022Interop.transformation.TransactionTranslator;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class Pain00100109ch03Test {
    @Test
    public void pain001ExamplePTC() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(ledger));
        ti.setTargetCcy(ledger.getNativeCcySymbol());

        ExchangeRate[] rates = {
                new ExchangeRate("USD", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
        };
        var r = new Pain001Reader(ledger);
        var tt = new TransactionTranslator(ti, new CurrencyConverter(rates));
        var transactions = tt.apply(r.read(getClass().getClassLoader().getResourceAsStream("pain001/pain00100109/ch03/pain_001_Example_PT_C.xml")));

        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(1, transactions.length);

        var expectedSenderAddress = new Address("MUSTER AG");

        var t = transactions[0];
        assertNull(t.getId());
        assertNull(t.getInvoiceId());
        assertNotNull(t.getMessages());
        Assertions.assertEquals(0, t.getMessages().length);
        Assertions.assertEquals(0, t.getStructuredReferences().length);
        Assertion.assertEqualsAccount(t, "CH5481230000001998736", null);
        Assertion.assertAmtCcy(t, 3421.0, "USD", 3421.0, "TEST");
        Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress);
        Assertion.assertEquals(t.getReceiverAddress(), new Address("Peter Fonda") {{
            setStreet("Saville Row 4");
            setZip("EC2R WYK");
            setCity("London");
            setCountryShort("GB");
        }});
    }
}
