package com.radynamics.dallipay.cryptoledger.generic.paymentpath;

import com.radynamics.dallipay.cryptoledger.MoneyBag;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;

import java.util.ArrayList;
import java.util.Arrays;

public final class BothHolding {
    public static Currency[] list(Wallet sender, Wallet receiver, Currency userCcy) {
        if (sender == null) throw new IllegalArgumentException("Parameter 'sender' cannot be null");
        if (receiver == null) throw new IllegalArgumentException("Parameter 'receiver' cannot be null");
        if (userCcy == null) throw new IllegalArgumentException("Parameter 'userCcy' cannot be null");

        var ccyAnyIssuerSender = findSameCode(sender.getBalances(), userCcy);
        // If expected currency code isn't available, there won't be an accepted one by both sender and receiver.
        if (ccyAnyIssuerSender == null) {
            return new Currency[0];
        }

        return findEqual(sender.getBalances(), receiver.getBalances(), ccyAnyIssuerSender);
    }

    private static Currency[] findEqual(MoneyBag first, MoneyBag second, Currency ccyAnyIssuer) {
        var firstCandidates = filter(first, ccyAnyIssuer);
        var secondCandidates = filter(second, ccyAnyIssuer);

        var candidates = new ArrayList<Currency>();
        for (var candidate : firstCandidates) {
            for (var secondCandidate : secondCandidates) {
                if (candidate.equals(secondCandidate)) {
                    candidates.add(candidate);
                }
            }
        }

        return candidates.toArray(new Currency[0]);
    }

    private static Currency[] filter(MoneyBag bag, Currency ccyAnyIssuer) {
        return Arrays.stream(bag.all()).map(Money::getCcy).filter(o -> o.sameCode(ccyAnyIssuer)).toArray(Currency[]::new);
    }

    private static Currency findSameCode(MoneyBag balance, com.radynamics.dallipay.exchange.Currency ccy) {
        var available = Arrays.stream(balance.all()).map(Money::getCcy).toArray(com.radynamics.dallipay.exchange.Currency[]::new);
        for (var o : available) {
            if (o.sameCode(ccy)) {
                return o;
            }
        }

        return null;
    }
}
