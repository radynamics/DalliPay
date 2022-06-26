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

public class Pain00100109Test {
    @Test
    public void readExample01() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(ledger));
        ti.setTargetCcy(ledger.getNativeCcySymbol());

        ExchangeRate[] rates = {
                new ExchangeRate("USD", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
                new ExchangeRate("TRY", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
        };
        var r = new Pain001Reader(ledger);
        var tt = new TransactionTranslator(ti, new CurrencyConverter(rates));
        var transactions = tt.apply(r.read(getClass().getClassLoader().getResourceAsStream("pain001/pain00100109/pain.001.001.09_example01.xml")));

        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(4, transactions.length);

        var expectedSenderAddress = new Address("SCHLIFFENBACHER JOSEF") {{
            setCity("84307 EGGENFELDEN");
            setCountryShort("DE");
        }};
        {
            var t = transactions[0];
            assertNull(t.getId());
            assertNull(t.getInvoiceId());
            assertNotNull(t.getMessages());
            Assertions.assertEquals(1, t.getMessages().length);
            Assertions.assertEquals("UEBERWEISUNG AUF DAS KONTO DES BEGU NSTIGTEN Zahlung lt. Avis", t.getMessages()[0]);
            Assertions.assertEquals(0, t.getStructuredReferences().length);
            Assertion.assertEqualsAccount(t, "DE14740618130000033626", "545445545455");
            Assertion.assertAmtCcy(t, 50.0, "USD", 50.0, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress);
            Assertion.assertEquals(t.getReceiverAddress(), new Address("XDR CAHNGZHOU LIMITED") {{
                setStreet("LVCHENG ROAD");
                setCity("CN-548765 CHANGZHOU");
                setCountryShort("CN");
            }});
        }
        {
            var t = transactions[1];
            assertNull(t.getId());
            assertNull(t.getInvoiceId());
            assertNotNull(t.getMessages());
            Assertions.assertEquals(1, t.getMessages().length);
            Assertions.assertEquals("ZAHLUNG FUER OEL Zahlung lt. Avis", t.getMessages()[0]);
            Assertions.assertEquals(0, t.getStructuredReferences().length);
            Assertion.assertEqualsAccount(t, "DE14740618130000033626", "KW81CBKU0000000000001234560101");
            Assertion.assertAmtCcy(t, 650.0, "USD", 650.0, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress);
            Assertion.assertEquals(t.getReceiverAddress(), new Address("UTTENDORFER WILLY") {{
                setStreet("GREAT PLACE");
                setCity("KW-45645 KUWAIT CITY");
                setCountryShort("KW");
            }});
        }
        {
            var t = transactions[2];
            assertNull(t.getId());
            assertNull(t.getInvoiceId());
            assertNotNull(t.getMessages());
            Assertions.assertEquals(1, t.getMessages().length);
            Assertions.assertEquals("LT. AVIS VOM 17,25,2020 Zahlung lt. Avis", t.getMessages()[0]);
            Assertions.assertEquals(0, t.getStructuredReferences().length);
            Assertion.assertEqualsAccount(t, "DE14740618130000033626", "545445545455");
            Assertion.assertAmtCcy(t, 200.0, "USD", 200.0, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress);
            Assertion.assertEquals(t.getReceiverAddress(), new Address("XDR CAHNGZHOU LIMITED") {{
                setStreet("LVCHENG ROAD");
                setCity("CN-548765 CHANGZHOU");
                setCountryShort("CN");
            }});
        }
        {
            var t = transactions[3];
            assertNull(t.getId());
            assertNull(t.getInvoiceId());
            assertNotNull(t.getMessages());
            Assertions.assertEquals(1, t.getMessages().length);
            Assertions.assertEquals("RECHNUNG 456123 VOM 25,03,2020 Zahlung lt. Avis", t.getMessages()[0]);
            Assertions.assertEquals(0, t.getStructuredReferences().length);
            Assertion.assertEqualsAccount(t, "DE14740618130000033626", "TR330006100519786457841326");
            Assertion.assertAmtCcy(t, 250.0, "TRY", 250.0, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), expectedSenderAddress);
            Assertion.assertEquals(t.getReceiverAddress(), new Address("WHEEL INDUSTRY") {{
                setCity("TR-ANKARA");
                setCountryShort("TR");
            }});
        }
    }

    @Test
    public void readExample02() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(ledger));
        ti.setTargetCcy(ledger.getNativeCcySymbol());

        ExchangeRate[] rates = {
                new ExchangeRate("EUR", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
        };
        var r = new Pain001Reader(ledger);
        var tt = new TransactionTranslator(ti, new CurrencyConverter(rates));
        var transactions = tt.apply(r.read(getClass().getClassLoader().getResourceAsStream("pain001/pain00100109/pain.001.001.09_example02.xml")));

        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(3, transactions.length);

        {
            var t = transactions[0];
            Assertions.assertNull(t.getId());
            Assertions.assertNull(t.getInvoiceId());
            Assertions.assertNotNull(t.getMessages());
            Assertions.assertEquals(1, t.getMessages().length);
            Assertions.assertEquals("Nur Scheckzahlung m\u00f6glich", t.getMessages()[0]);
            Assertions.assertEquals(0, t.getStructuredReferences().length);
            Assertion.assertEqualsAccount(t, "DE14740618130000033626", null);
            Assertion.assertAmtCcy(t, 5000.0, "EUR", 5000.0, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), new Address("Braun Mathias") {{
                setStreet("Oxford Road 456");
                setZip("554554");
                setCity("London");
                setCountryShort("GB");
            }});
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Fraunhofer Franz") {{
                setStreet("Times Quare 446578");
                setZip("84307");
                setCity("New York");
                setCountryShort("US");
            }});
        }
        {
            var t = transactions[1];
            Assertions.assertNull(t.getId());
            Assertions.assertNull(t.getInvoiceId());
            Assertions.assertNotNull(t.getMessages());
            Assertions.assertEquals(0, t.getMessages().length);
            Assertions.assertEquals(1, t.getStructuredReferences().length);
            Assertions.assertEquals(ReferenceType.Scor, t.getStructuredReferences()[0].getType());
            Assertions.assertEquals("Ref 455244", t.getStructuredReferences()[0].getUnformatted());
            Assertion.assertEqualsAccount(t, "DE14740618130000033626", "50004564552");
            Assertion.assertAmtCcy(t, 4521.32, "EUR", 4521.32, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), new Address("Maier Bau GmbH") {{
                setStreet("Bergstra\u00DFe 35");
                setZip("84347");
                setCity("Pfarrkrichen");
                setCountryShort("DE");
            }});
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Changzhou Limited") {{
                setStreet("Lvcheng Road 865");
                setZip("213169");
                setCity("Changzhou");
                setCountryShort("CN");
            }});
        }
        {
            var t = transactions[2];
            Assertions.assertNull(t.getId());
            Assertions.assertNull(t.getInvoiceId());
            Assertions.assertNotNull(t.getMessages());
            Assertions.assertEquals(1, t.getMessages().length);
            Assertions.assertEquals("Verwendungszweck 1 Verwendungszweck2", t.getMessages()[0]);
            Assertions.assertEquals(0, t.getStructuredReferences().length);
            Assertion.assertEqualsAccount(t, "DE14740618130000033626", "KW81CBKU0000000000001234560101");
            Assertion.assertAmtCcy(t, 1234.56, "EUR", 1234.56, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), new Address("Maier Bau GmbH") {{
                setStreet("Bergstra\u00DFe 35");
                setZip("84347");
                setCity("Pfarrkrichen");
                setCountryShort("DE");
            }});
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Uttendorfer Willy") {{
                setStreet("Great Place 45");
                setZip("4567");
                setCity("Kuwait City");
                setCountryShort("KW");
            }});
        }
    }

    @Test
    public void readExamplePTXv1PTS() throws Exception {
        var ledger = new TestLedger();
        var ti = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(ledger));
        ti.setTargetCcy(ledger.getNativeCcySymbol());

        ExchangeRate[] rates = {
                new ExchangeRate("EUR", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
                new ExchangeRate("USD", ledger.getNativeCcySymbol(), 1, ZonedDateTime.now()),
        };
        var r = new Pain001Reader(ledger);
        var tt = new TransactionTranslator(ti, new CurrencyConverter(rates));
        var transactions = tt.apply(r.read(getClass().getClassLoader().getResourceAsStream("pain001/pain00100109/pain_001_Example_PT_X_V1_PT_S.xml")));

        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(3, transactions.length);

        {
            var t = transactions[0];
            Assertions.assertNull(t.getId());
            Assertions.assertNull(t.getInvoiceId());
            Assertions.assertNotNull(t.getMessages());
            Assertions.assertEquals(0, t.getMessages().length);
            Assertions.assertEquals(1, t.getStructuredReferences().length);
            Assertions.assertEquals(ReferenceType.Scor, t.getStructuredReferences()[0].getType());
            Assertions.assertEquals("RF4220210323103704APG0018", t.getStructuredReferences()[0].getUnformatted());
            Assertion.assertEqualsAccount(t, "CH7280005000088877766", "CH5021977000004331346");
            Assertion.assertAmtCcy(t, 3949.75, "USD", 3949.75, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), new Address("MUSTER AG") {{
                setCity("SELDWYLA");
                setCountryShort("CH");
            }});
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Peter Haller") {{
                setStreet("Rosenauweg 4");
                setZip("8036");
                setCity("Z\u00fcrich");
                setCountryShort("CH");
            }});
        }
        {
            var t = transactions[1];
            Assertions.assertNull(t.getId());
            Assertions.assertNull(t.getInvoiceId());
            Assertions.assertNotNull(t.getMessages());
            Assertions.assertEquals(1, t.getMessages().length);
            Assertions.assertEquals("Rechnung Nr. 408", t.getMessages()[0]);
            Assertions.assertEquals(0, t.getStructuredReferences().length);
            Assertion.assertEqualsAccount(t, "CH7280005000088877766", "CH4221988000009522865");
            Assertion.assertAmtCcy(t, 8479.25, "EUR", 8479.25, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), new Address("MUSTER AG"));
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Robert Scheider SA") {{
                setStreet("Rue de la gare 24");
                setZip("2501");
                setCity("Biel");
                setCountryShort("CH");
            }});
        }
        {
            var t = transactions[2];
            Assertions.assertNull(t.getId());
            Assertions.assertNull(t.getInvoiceId());
            Assertions.assertNotNull(t.getMessages());
            Assertions.assertEquals(0, t.getMessages().length);
            Assertions.assertEquals(1, t.getStructuredReferences().length);
            Assertions.assertEquals(ReferenceType.Scor, t.getStructuredReferences()[0].getType());
            Assertions.assertEquals("RF712348231", t.getStructuredReferences()[0].getUnformatted());
            Assertion.assertEqualsAccount(t, "CH7280005000088877766", "DE62007620110623852957");
            Assertion.assertAmtCcy(t, 3421.00, "EUR", 3421.00, "TEST");
            Assertion.assertEquals(t.getSenderAddress(), new Address("MUSTER AG"));
            Assertion.assertEquals(t.getReceiverAddress(), new Address("Peter Haller") {{
                setStreet("Rosenauweg 4");
                setZip("8036");
                setCity("Z\u00fcrich");
                setCountryShort("CH");
            }});
        }
    }
}
