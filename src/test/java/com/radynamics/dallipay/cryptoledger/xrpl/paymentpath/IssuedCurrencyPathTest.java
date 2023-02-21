package com.radynamics.dallipay.cryptoledger.xrpl.paymentpath;

import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyFormatter;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.pain001.Assertion;
import com.radynamics.dallipay.iso20022.pain001.TestLedger;
import com.radynamics.dallipay.iso20022.pain001.TestTransaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IssuedCurrencyPathTest {
    private final CurrencyFormatter ccyFormatter = new CurrencyFormatter(new WalletInfoProvider[0]);

    @Test
    public void ctrArgsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new IssuedCurrencyPath(null, null, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new IssuedCurrencyPath(ccyFormatter, null, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new IssuedCurrencyPath(null, new Currency("TEST"), -1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new IssuedCurrencyPath(ccyFormatter, new Currency("TEST"), 0));
    }

    @Test
    public void apply() {
        var ledger = new TestLedger();
        var ccyCCC = new Currency("CCC", ledger.createWallet("CCC_issuer", ""));
        
        var p = new Payment(new TestTransaction(ledger, 10.0, "TEST"));
        p.setAmount(Money.of(20.0, ccyCCC));
        var path = new IssuedCurrencyPath(ccyFormatter, ccyCCC, 0);
        path.apply(p);

        Assertions.assertNull(p.getExchangeRate());
        Assertions.assertEquals(20.0, p.getAmount());
        Assertions.assertEquals("CCC", p.getUserCcyCodeOrEmpty());
        Assertion.assertEquals(Money.of(20.0, ccyCCC), p.getAmountTransaction());
    }
}
