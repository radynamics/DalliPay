package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.Assertion;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestLedger;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestTransaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CurrencyRefresherTest {
    @Test
    public void refreshArgNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CurrencyRefresher().refresh(null);
        });
    }

    @ParameterizedTest
    @CsvSource({",", "aaa,", ",bbb"})
    public void refreshWalletsNull(String senderWallet, String receiverWallet) {
        var amt = Money.of(10.0, new Currency("TEST"));
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
        p.setSenderWallet(senderWallet == null ? null : p.getLedger().createWallet(senderWallet, ""));
        p.setReceiverWallet(receiverWallet == null ? null : p.getLedger().createWallet(receiverWallet, ""));
        p.setAmount(amt);
        new CurrencyRefresher().refresh(p);

        Assertions.assertNull(p.getExchangeRate());
        Assertion.assertEqual(amt, p.getAmountTransaction());
    }
}
