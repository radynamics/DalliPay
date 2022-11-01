package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.paymentpath;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerNativeCcyPath;
import com.radynamics.CryptoIso20022Interop.cryptoledger.PaymentPath;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.TestUtils;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestLedger;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestTransaction;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;

public class PaymentPathFinderTest {
    private final Ledger ledger = new TestLedger();

    @Test
    public void findArgsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PaymentPathFinder().find(null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PaymentPathFinder().find(new CurrencyConverter(), null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PaymentPathFinder().find(null, new Payment(ledger.createTransaction())));
    }

    @ParameterizedTest
    @CsvSource({",", "aaa,", ",bbb"})
    public void findWalletsNull(String senderWallet, String receiverWallet) {
        var amt = Money.of(10.0, new Currency("TEST"));
        var p = new Payment(new TestTransaction(ledger, 10.0, "TEST"));
        p.setSenderWallet(senderWallet == null ? null : ledger.createWallet(senderWallet, ""));
        p.setReceiverWallet(receiverWallet == null ? null : ledger.createWallet(receiverWallet, ""));
        p.setAmount(amt);
        var actual = new PaymentPathFinder().find(new CurrencyConverter(), p);

        assertSingleLedgerNativeCcyPath(actual);
    }

    private void assertSingleLedgerNativeCcyPath(PaymentPath[] actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.length);
        assertLedgerNativeCcyPath(actual[0]);
    }

    private void assertLedgerNativeCcyPath(PaymentPath actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertInstanceOf(LedgerNativeCcyPath.class, actual);
        Assertions.assertEquals(((LedgerNativeCcyPath) actual).getCcy(), new Currency("TEST"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AAA", "BBB", "CCC"})
    public void findNoCommonTrustlines(@NotNull String userCcyCode) {
        var ccyAAA = TestUtils.createIssuedCcy(ledger, "AAA");
        var ccyBBB = TestUtils.createIssuedCcy(ledger, "BBB");
        var ccyCCC1 = TestUtils.createIssuedCcy(ledger, "CCC", "CCC_issuer1");
        var ccyCCC2 = TestUtils.createIssuedCcy(ledger, "CCC", "CCC_issuer2");

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
        var actual = new PaymentPathFinder().find(new CurrencyConverter(), p);

        assertSingleLedgerNativeCcyPath(actual);
    }

    @Test
    public void findCommonTrustlines() {
        var ccyAAA = TestUtils.createIssuedCcy(ledger, "AAA");
        var ccyBBB = TestUtils.createIssuedCcy(ledger, "BBB");
        var ccyCCC = TestUtils.createIssuedCcy(ledger, "CCC");

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
        var actual = new PaymentPathFinder().find(new CurrencyConverter(), p);

        Assertions.assertEquals(2, actual.length);
        assertLedgerNativeCcyPath(actual[0]);

        Assertions.assertInstanceOf(IssuedCurrencyPath.class, actual[1]);
        Assertions.assertEquals(((IssuedCurrencyPath) actual[1]).getCcy(), ccyCCC);
        Assertions.assertEquals(actual[1].getRank(), 10);
    }

    @Test
    public void findCommonTrustlinesTransferFee() {
        var ccyAAA = TestUtils.createIssuedCcy(ledger, "AAA");
        var ccyBBB = TestUtils.createIssuedCcy(ledger, "BBB");
        var ccyCCC1 = TestUtils.createIssuedCcy(ledger, "CCC", "CCC_issuer1");
        ccyCCC1.setTransferFee(3);
        var ccyCCC2 = TestUtils.createIssuedCcy(ledger, "CCC", "CCC_issuer2");
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
        var actual = new PaymentPathFinder().find(new CurrencyConverter(), p);

        Assertions.assertEquals(3, actual.length);
        assertLedgerNativeCcyPath(actual[0]);

        {
            var actualPath = actual[1];
            Assertions.assertInstanceOf(IssuedCurrencyPath.class, actualPath);
            // CCC_issuer2 has lower transfer fee
            Assertions.assertEquals(((IssuedCurrencyPath) actualPath).getCcy(), ccyCCC2);
            Assertions.assertEquals(actualPath.getRank(), 10);
        }
        {
            var actualPath = actual[2];
            Assertions.assertInstanceOf(IssuedCurrencyPath.class, actualPath);
            Assertions.assertEquals(((IssuedCurrencyPath) actualPath).getCcy(), ccyCCC1);
            Assertions.assertEquals(actualPath.getRank(), 9);
        }
    }
}
