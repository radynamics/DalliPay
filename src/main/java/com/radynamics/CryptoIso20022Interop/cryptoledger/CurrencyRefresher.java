package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class CurrencyRefresher {
    public void refresh(Payment p) {
        if (p == null) throw new IllegalArgumentException("Parameter 'p' cannot be null");

        var senderValid = WalletValidator.isValidFormat(p.getLedger(), p.getSenderWallet());
        var receiverValid = WalletValidator.isValidFormat(p.getLedger(), p.getReceiverWallet());

        // Use ledger native currency if information about sender/receiver is missing
        if (!senderValid || !receiverValid) {
            setAmountLedgerUnit(p);
            return;
        }

        var ccyAnyIssuerSender = findSameCode(p.getSenderWallet().getBalances(), p.getUserCcy());
        // Use ledger native currency if expected currency code isn't available
        if (ccyAnyIssuerSender == null) {
            setAmountLedgerUnit(p);
            return;
        }

        var ccyBothAccepting = findEqual(p.getSenderWallet().getBalances(), p.getReceiverWallet().getBalances(), ccyAnyIssuerSender);
        // Use ledger native currency if expected currency isn't available on both sides
        if (ccyBothAccepting == null) {
            setAmountLedgerUnit(p);
            return;
        }

        p.setAmount(Money.of(p.getAmount(), ccyBothAccepting));
    }

    private void setAmountLedgerUnit(Payment p) {
        p.setExchangeRate(null);
        p.setAmount(Money.of(p.getAmount(), p.getUserCcy().withoutIssuer()));
    }

    private static Currency findEqual(MoneyBag first, MoneyBag second, Currency ccyAnyIssuer) {
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

        if (candidates.size() == 0) {
            return null;
        }

        // Choose lowest fee
        Collections.sort(candidates, Comparator.comparing(Currency::getTransferFee));
        return candidates.get(0);
    }

    private static Currency[] filter(MoneyBag bag, Currency ccyAnyIssuer) {
        return Arrays.stream(bag.all()).map(Money::getCcy).filter(o -> o.sameCode(ccyAnyIssuer)).toArray(Currency[]::new);
    }

    private static Currency findSameCode(MoneyBag balance, Currency ccy) {
        var available = Arrays.stream(balance.all()).map(Money::getCcy).toArray(Currency[]::new);
        for (var o : available) {
            if (o.sameCode(ccy)) {
                return o;
            }
        }

        return null;
    }
}
