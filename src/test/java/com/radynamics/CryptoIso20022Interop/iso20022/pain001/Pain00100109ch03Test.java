package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.Config;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.Address;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
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

    @Test
    public void pain001ExamplePTDQrrScor() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(ledger));
        ti.setTargetCcy(ledger.getNativeCcySymbol());

        ExchangeRate[] rates = {
                new ExchangeRate("CHF", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
                new ExchangeRate("EUR", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
        };
        var r = new Pain001Reader(ledger);
        var tt = new TransactionTranslator(ti, new CurrencyConverter(rates));
        var transactions = tt.apply(r.read(getClass().getClassLoader().getResourceAsStream("pain001/pain00100109/ch03/pain_001_Example_PT_D_QRR_SCOR.xml")));

        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.length);

        var expectedSenderAddress = new Address("MUSTER AG") {{
            setCity("SELDWYLA");
            setCountryShort("CH");
        }};
        {
            var t = transactions[0];
            assertNull(t.getId());
            assertNull(t.getInvoiceId());
            assertNotNull(t.getMessages());
            Assertions.assertEquals(1, t.getMessages().length);
            Assertions.assertEquals("Auftrag vom 10.02.2023", t.getMessages()[0]);
            Assertions.assertEquals(1, t.getStructuredReferences().length);
            Assertions.assertEquals(ReferenceType.SwissQrBill, t.getStructuredReferences()[0].getType());
            Assertions.assertEquals("210000000003139471430009017", t.getStructuredReferences()[0].getUnformatted());
            Assertion.assertEqualsAccount(t, "CH7280005000088877766", "CH4431999123000889012");
            Assertion.assertAmtCcy(t, 3949.75, "CHF", 3949.75, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress);
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Robert Schneider AG") {{
                setStreet("Rue du Lac 1268");
                setZip("2501");
                setCity("Biel");
                setCountryShort("CH");
            }});
        }
        {
            var t = transactions[1];
            assertNull(t.getId());
            assertNull(t.getInvoiceId());
            assertNotNull(t.getMessages());
            Assertions.assertEquals(0, t.getMessages().length);
            Assertions.assertEquals(1, t.getStructuredReferences().length);
            Assertions.assertEquals(ReferenceType.Scor, t.getStructuredReferences()[0].getType());
            Assertions.assertEquals("RF18539007547034", t.getStructuredReferences()[0].getUnformatted());
            Assertion.assertEqualsAccount(t, "CH7280005000088877766", "CH4821966000009613388");
            Assertion.assertAmtCcy(t, 199.95, "EUR", 199.95, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress);
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Peter Haller") {{
                setStreet("Rosenauwegc 4");
                setZip("8036");
                setCity("Z\u00fcrich");
                setCountryShort("CH");
            }});
        }
    }

    @Test
    public void pain001ExamplePTS() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(ledger));
        ti.setTargetCcy(ledger.getNativeCcySymbol());

        ExchangeRate[] rates = {
                new ExchangeRate("EUR", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
        };
        var r = new Pain001Reader(ledger);
        var tt = new TransactionTranslator(ti, new CurrencyConverter(rates));
        var transactions = tt.apply(r.read(getClass().getClassLoader().getResourceAsStream("pain001/pain00100109/ch03/pain_001_Example_PT_S.xml")));

        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(1, transactions.length);

        var expectedSenderAddress = new Address("MUSTER AG");

        var t = transactions[0];
        assertNull(t.getId());
        assertNull(t.getInvoiceId());
        assertNotNull(t.getMessages());
        Assertions.assertEquals(0, t.getMessages().length);
        Assertions.assertEquals(1, t.getStructuredReferences().length);
        Assertions.assertEquals(ReferenceType.Scor, t.getStructuredReferences()[0].getType());
        Assertions.assertEquals("RF712348231", t.getStructuredReferences()[0].getUnformatted());
        Assertion.assertEqualsAccount(t, "CH5481230000001998736", "DE62007620110623852957");
        Assertion.assertAmtCcy(t, 3421.0, "EUR", 3421.0, "TEST");
        Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress);
        Assertion.assertEquals(t.getReceiverAddress(), new Address("Peter Haller") {{
            setStreet("Rosenauweg 4");
            setCity("DE-80036 M\u00fcnchen");
            setCountryShort("DE");
        }});
    }
}
