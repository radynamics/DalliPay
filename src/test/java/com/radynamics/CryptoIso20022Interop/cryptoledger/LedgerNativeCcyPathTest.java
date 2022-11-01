package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.TestUtils;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.Assertion;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestLedger;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestTransaction;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;

public class LedgerNativeCcyPathTest {
    @Test
    public void ctrArgsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LedgerNativeCcyPath(null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LedgerNativeCcyPath(new CurrencyConverter(), null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LedgerNativeCcyPath(null, new Currency("TEST")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AAA", "BBB", "CCC"})
    public void apply(@NotNull String userCcyCode) {
        var ledger = new TestLedger();
        var ccyAAA = TestUtils.createIssuedCcy(ledger, "AAA");
        var ccyBBB = TestUtils.createIssuedCcy(ledger, "BBB");
        var ccyCCC = TestUtils.createIssuedCcy(ledger, "CCC");

        var map = new HashMap<String, Currency>();
        map.put("AAA", ccyAAA);
        map.put("BBB", ccyBBB);
        map.put("CCC", ccyCCC);

        var p = new Payment(new TestTransaction(ledger, 10.0, "TEST"));
        p.setAmount(Money.of(20.0, map.get(userCcyCode)));
        var path = new LedgerNativeCcyPath(new CurrencyConverter(), new Currency("TEST"));
        path.apply(p);

        Assertions.assertNull(p.getExchangeRate());
        Assertions.assertEquals(20.0, p.getAmount());
        Assertions.assertEquals(userCcyCode, p.getUserCcyCodeOrEmpty());
        Assertion.assertEquals(Money.of(0, new Currency("TEST")), p.getAmountTransaction());
    }
}
