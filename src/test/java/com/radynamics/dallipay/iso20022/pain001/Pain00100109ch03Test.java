package com.radynamics.dallipay.iso20022.pain001;

import com.radynamics.dallipay.Config;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.iso20022.Address;
import com.radynamics.dallipay.iso20022.creditorreference.ReferenceType;
import com.radynamics.dallipay.transformation.MemoryAccountMappingSource;
import com.radynamics.dallipay.transformation.TransactionTranslator;
import com.radynamics.dallipay.transformation.TransformInstruction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class Pain00100109ch03Test {
    @Test
    public void pain001ExamplePTC() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(false));
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
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(false));
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
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(false));
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

    @Test
    public void pain001ExamplePTXV1PTS() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(false));
        ti.setTargetCcy(ledger.getNativeCcySymbol());

        ExchangeRate[] rates = {
                new ExchangeRate("USD", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
                new ExchangeRate("EUR", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
        };
        var r = new Pain001Reader(ledger);
        var tt = new TransactionTranslator(ti, new CurrencyConverter(rates));
        var transactions = tt.apply(r.read(getClass().getClassLoader().getResourceAsStream("pain001/pain00100109/ch03/pain_001_Example_PT_X_V1_PT_S.xml")));

        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(3, transactions.length);

        var expectedSenderAddress0 = new Address("MUSTER AG") {{
            setCity("SELDWYLA");
            setCountryShort("CH");
        }};
        var expectedSenderAddress1 = new Address("MUSTER AG");
        {
            var t = transactions[0];
            assertNull(t.getId());
            assertNull(t.getInvoiceId());
            assertNotNull(t.getMessages());
            Assertions.assertEquals(0, t.getMessages().length);
            Assertions.assertEquals(1, t.getStructuredReferences().length);
            Assertions.assertEquals(ReferenceType.Scor, t.getStructuredReferences()[0].getType());
            Assertions.assertEquals("RF4220210323103704APG0018", t.getStructuredReferences()[0].getUnformatted());
            Assertion.assertEqualsAccount(t, "CH7280005000088877766", "CH5021977000004331346");
            Assertion.assertAmtCcy(t, 3949.75, "USD", 3949.75, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress0);
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Peter Haller") {{
                setStreet("Rosenauweg 4");
                setZip("8036");
                setCity("Z\u00fcrich");
                setCountryShort("CH");
            }});
        }
        {
            var t = transactions[1];
            assertNull(t.getId());
            assertNull(t.getInvoiceId());
            assertNotNull(t.getMessages());
            Assertions.assertEquals(1, t.getMessages().length);
            Assertions.assertEquals("Rechnung Nr. 408", t.getMessages()[0]);
            Assertions.assertEquals(0, t.getStructuredReferences().length);
            Assertion.assertEqualsAccount(t, "CH7280005000088877766", "CH4221988000009522865");
            Assertion.assertAmtCcy(t, 8479.25, "EUR", 8479.25, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress1);
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Robert Scheider SA") {{
                setStreet("Rue de la gare 24");
                setZip("2501");
                setCity("Biel");
                setCountryShort("CH");
            }});
        }
        {
            var t = transactions[2];
            assertNull(t.getId());
            assertNull(t.getInvoiceId());
            assertNotNull(t.getMessages());
            Assertions.assertEquals(0, t.getMessages().length);
            Assertions.assertEquals(1, t.getStructuredReferences().length);
            Assertions.assertEquals(ReferenceType.Scor, t.getStructuredReferences()[0].getType());
            Assertions.assertEquals("RF712348231", t.getStructuredReferences()[0].getUnformatted());
            Assertion.assertEqualsAccount(t, "CH7280005000088877766", "DE62007620110623852957");
            Assertion.assertAmtCcy(t, 3421.0, "EUR", 3421.0, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress1);
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Peter Haller") {{
                setStreet("Rosenauweg 4");
                setZip("8036");
                setCity("Z\u00fcrich");
                setCountryShort("CH");
            }});
        }
    }

    @Test
    public void pain001ExamplePTXV2() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(false));
        ti.setTargetCcy(ledger.getNativeCcySymbol());

        ExchangeRate[] rates = {
                new ExchangeRate("USD", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
        };
        var r = new Pain001Reader(ledger);
        var tt = new TransactionTranslator(ti, new CurrencyConverter(rates));
        var transactions = tt.apply(r.read(getClass().getClassLoader().getResourceAsStream("pain001/pain00100109/ch03/pain_001_Example_PT_X_V2.xml")));

        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(1, transactions.length);

        var expectedSenderAddress = new Address("MUSTER AG") {{
            setCity("SELDWYLA");
            setCountryShort("CH");
        }};
        var t = transactions[0];
        assertNull(t.getId());
        assertNull(t.getInvoiceId());
        assertNotNull(t.getMessages());
        Assertions.assertEquals(0, t.getMessages().length);
        Assertions.assertEquals(1, t.getStructuredReferences().length);
        Assertions.assertEquals(ReferenceType.Scor, t.getStructuredReferences()[0].getType());
        Assertions.assertEquals("RF712348231", t.getStructuredReferences()[0].getUnformatted());
        Assertion.assertEqualsAccount(t, "CH5481230000001998736", "474-8512-007");
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
    public void pain001AcctWalletAddress() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(false));
        ti.setTargetCcy(ledger.getNativeCcySymbol());

        ExchangeRate[] rates = {
                new ExchangeRate("USD", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
        };
        var r = new Pain001Reader(ledger);
        var tt = new TransactionTranslator(ti, new CurrencyConverter(rates));
        var transactions = tt.apply(r.read(getClass().getClassLoader().getResourceAsStream("pain001/pain00100109/ch03/pain_001_Example_AcctWalletAddress.xml")));

        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(1, transactions.length);

        var t = transactions[0];
        Assertion.assertEqualsWallet(t, "rwYb1M4hZcSG6tcAuhvgEwSpsiACKv6BG8", "rNZtEviqTua4FcJebLkhq9hS7fkuxaodya");
    }
}
