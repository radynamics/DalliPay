package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.iso20022.pain001.TestLedger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URI;

public class PaymentRequestUriTest {
    @Test
    public void ctrNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PaymentRequestUri(null));
    }

    @Test
    public void matches() {
        Assertions.assertTrue(PaymentRequestUri.matches("http://127.0.0.1:58909/request/?to=aBc"));
        Assertions.assertTrue(PaymentRequestUri.matches("http://127.0.0.1:58909/request/?to=aBc&dt=123"));
        Assertions.assertTrue(PaymentRequestUri.matches("http://127.0.0.1:58909/request/?to=aBc&amount=98.7654321"));
        Assertions.assertTrue(PaymentRequestUri.matches("http://127.0.0.1:58909/request/?to=aBc&amount=98.76&currency=USD"));
        Assertions.assertTrue(PaymentRequestUri.matches("http://127.0.0.1:58909/request/?to=aBc&amount=98.76&currency=usd"));
        Assertions.assertTrue(PaymentRequestUri.matches("http://127.0.0.1:58909/request/?to=aBc&amount=98.76&currency=USD&refno=RF18539007547034"));
        Assertions.assertTrue(PaymentRequestUri.matches("http://127.0.0.1:58909/request/?to=aBc&amount=98.76&currency=USD&refno=RF18539007547034&msg=test"));

        Assertions.assertFalse(PaymentRequestUri.matches("http://127.0.0.1:58909/request/?amount=98.7654321"));
        Assertions.assertFalse(PaymentRequestUri.matches("http://127.0.0.1:58909/request/?dt=123"));
    }

    @Test
    public void createOrNull() {
        var ledger = new TestLedger();
        {
            var o = new PaymentRequestUri(ledger).createOrNull(URI.create("http://127.0.0.1:58909/request/?to=aBc"));
            Assertions.assertEquals("aBc", o.getReceiverWallet().getPublicKey());
            Assertions.assertEquals(0.0, o.getAmount());
            Assertions.assertEquals("TEST", o.getUserCcyCodeOrEmpty());
            Assertions.assertEquals(0.0, o.getAmountTransaction().getNumber());
            Assertions.assertEquals("TEST", o.getAmountTransaction().getCcy().getCode());
        }
        {
            var o = new PaymentRequestUri(ledger).createOrNull(URI.create("http://127.0.0.1:58909/request/?to=aBc&amount=98.76&currency=USD"));
            Assertions.assertEquals("aBc", o.getReceiverWallet().getPublicKey());
            Assertions.assertEquals(98.76, o.getAmount());
            Assertions.assertEquals("USD", o.getUserCcyCodeOrEmpty());
            // 0 due no exchange rate is set
            Assertions.assertEquals(0.0, o.getAmountTransaction().getNumber());
            Assertions.assertEquals("TEST", o.getAmountTransaction().getCcy().getCode());
        }
        {
            var o = new PaymentRequestUri(ledger).createOrNull(URI.create("http://127.0.0.1:58909/request/?to=aBc&dt=123&amount=98.7654321"));
            Assertions.assertEquals("aBc", o.getReceiverWallet().getPublicKey());
            Assertions.assertEquals("123", o.getDestinationTag());
            Assertions.assertEquals(98.7654321, o.getAmount());
            Assertions.assertEquals("TEST", o.getUserCcyCodeOrEmpty());
            Assertions.assertEquals(98.7654321, o.getAmountTransaction().getNumber());
            Assertions.assertEquals("TEST", o.getAmountTransaction().getCcy().getCode());
        }
        {
            var o = new PaymentRequestUri(ledger).createOrNull(URI.create("http://127.0.0.1:58909/request/?to=aBc&amount=98.76&currency=USD&refno=RF18539007547034&msg=test"));
            Assertions.assertEquals("aBc", o.getReceiverWallet().getPublicKey());
            Assertions.assertEquals(98.76, o.getAmount());
            Assertions.assertEquals("USD", o.getUserCcyCodeOrEmpty());
            // 0 due no exchange rate is set
            Assertions.assertEquals(0.0, o.getAmountTransaction().getNumber());
            Assertions.assertEquals("TEST", o.getAmountTransaction().getCcy().getCode());
            Assertions.assertEquals(1, o.getStructuredReferences().length);
            Assertions.assertEquals("RF18539007547034", o.getStructuredReferences()[0].getUnformatted());
            Assertions.assertEquals(1, o.getMessages().length);
            Assertions.assertEquals("test", o.getMessages()[0]);
        }
    }

    @ParameterizedTest
    @CsvSource({"USD", "usd"})
    public void createOrNullCcyCasing(String ccy) {
        var ledger = new TestLedger();

        var o = new PaymentRequestUri(ledger).createOrNull(URI.create("http://127.0.0.1:58909/request/?to=aBc&amount=98.76&currency=" + ccy));
        Assertions.assertEquals(98.76, o.getAmount());
        Assertions.assertEquals("USD", o.getUserCcyCodeOrEmpty());
    }
}
