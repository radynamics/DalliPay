package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.Assertion;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestLedger;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestTransaction;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;

public class CurrencyRefresherTest {
    private final Ledger ledger = new TestLedger();

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
        var p = new Payment(new TestTransaction(ledger, 10.0, "TEST"));
        p.setSenderWallet(senderWallet == null ? null : ledger.createWallet(senderWallet, ""));
        p.setReceiverWallet(receiverWallet == null ? null : ledger.createWallet(receiverWallet, ""));
        p.setAmount(amt);
        new CurrencyRefresher().refresh(p);

        Assertions.assertNull(p.getExchangeRate());
        Assertion.assertEqual(amt, p.getAmountTransaction());
    }

    @ParameterizedTest
    @ValueSource(strings = {"AAA", "BBB", "CCC"})
    public void refreshNoCommonTrustlines(@NotNull String userCcyCode) {
        var ccyAAA = createIssuedCcy("AAA");
        var ccyBBB = createIssuedCcy("BBB");
        var ccyCCC1 = createIssuedCcy("CCC", "CCC_issuer1");
        var ccyCCC2 = createIssuedCcy("CCC", "CCC_issuer2");

        var senderWallet = ledger.createWallet("aaa", "");
        senderWallet.getBalances().set(Money.of(80.0, ccyAAA));
        senderWallet.getBalances().set(Money.of(800.0, ccyCCC1));

        var receiverWallet = ledger.createWallet("bbb", "");
        receiverWallet.getBalances().set(Money.of(100.0, ccyBBB));
        receiverWallet.getBalances().set(Money.of(1000.0, ccyCCC2));

        var p = new Payment(new TestTransaction(ledger, 10.0, "TEST"));
        p.setSenderWallet(senderWallet);
        p.setReceiverWallet(receiverWallet);
        var map = new HashMap<String, Currency>();
        map.put("AAA", ccyAAA);
        map.put("BBB", ccyBBB);
        map.put("CCC", ccyCCC1);
        p.setAmount(Money.of(20.0, map.get(userCcyCode)));
        new CurrencyRefresher().refresh(p);

        Assertions.assertNull(p.getExchangeRate());
        Assertions.assertEquals(20.0, p.getAmount());
        Assertions.assertEquals(userCcyCode, p.getUserCcyCodeOrEmpty());
        Assertion.assertEqual(Money.of(0, new Currency("TEST")), p.getAmountTransaction());
    }

    private Currency createIssuedCcy(String ccyCode) {
        return createIssuedCcy(ccyCode, ccyCode + "_issuer");
    }

    private Currency createIssuedCcy(String ccyCode, String issuer) {
        return new Currency(ccyCode, ledger.createWallet(issuer, ""));
    }

    @Test
    public void refreshCommonTrustlines() {
        var ccyAAA = createIssuedCcy("AAA");
        var ccyBBB = createIssuedCcy("BBB");
        var ccyCCC = createIssuedCcy("CCC");

        var senderWallet = ledger.createWallet("aaa", "");
        senderWallet.getBalances().set(Money.of(80.0, ccyAAA));
        senderWallet.getBalances().set(Money.of(800.0, ccyCCC));

        var receiverWallet = ledger.createWallet("bbb", "");
        receiverWallet.getBalances().set(Money.of(100.0, ccyBBB));
        receiverWallet.getBalances().set(Money.of(1000.0, ccyCCC));

        var p = new Payment(new TestTransaction(ledger, 10.0, "TEST"));
        p.setSenderWallet(senderWallet);
        p.setReceiverWallet(receiverWallet);
        p.setAmount(Money.of(20.0, new Currency("CCC")));
        new CurrencyRefresher().refresh(p);

        Assertions.assertNull(p.getExchangeRate());
        Assertions.assertEquals(20.0, p.getAmount());
        Assertions.assertEquals("CCC", p.getUserCcyCodeOrEmpty());
        Assertion.assertEqual(Money.of(20.0, ccyCCC), p.getAmountTransaction());
    }

    @Test
    public void refreshCommonTrustlinesTransferFee() {
        var ccyAAA = createIssuedCcy("AAA");
        var ccyBBB = createIssuedCcy("BBB");
        var ccyCCC1 = createIssuedCcy("CCC", "CCC_issuer1");
        ccyCCC1.setTransferFee(3);
        var ccyCCC2 = createIssuedCcy("CCC", "CCC_issuer2");
        ccyCCC2.setTransferFee(2);

        var senderWallet = ledger.createWallet("aaa", "");
        senderWallet.getBalances().set(Money.of(80.0, ccyAAA));
        senderWallet.getBalances().set(Money.of(800.0, ccyCCC1));
        senderWallet.getBalances().set(Money.of(8000.0, ccyCCC2));

        var receiverWallet = ledger.createWallet("bbb", "");
        receiverWallet.getBalances().set(Money.of(100.0, ccyBBB));
        receiverWallet.getBalances().set(Money.of(1000.0, ccyCCC1));
        receiverWallet.getBalances().set(Money.of(10000.0, ccyCCC2));

        var p = new Payment(new TestTransaction(ledger, 10.0, "TEST"));
        p.setSenderWallet(senderWallet);
        p.setReceiverWallet(receiverWallet);
        p.setAmount(Money.of(20.0, new Currency("CCC")));
        new CurrencyRefresher().refresh(p);

        Assertions.assertNull(p.getExchangeRate());
        Assertions.assertEquals(20.0, p.getAmount());
        Assertions.assertEquals("CCC", p.getUserCcyCodeOrEmpty());
        Assertion.assertEqual(Money.of(20.0, new Currency("CCC")), p.getAmountTransaction());
        // CCC_issuer2 has lower transfer fee
        Assertions.assertEquals("CCC_issuer2", p.getAmountTransaction().getCcy().getIssuer().getPublicKey());
    }
}
