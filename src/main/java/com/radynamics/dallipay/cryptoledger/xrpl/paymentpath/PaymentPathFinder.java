package com.radynamics.dallipay.cryptoledger.xrpl.paymentpath;

import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.CurrencyFormatter;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class PaymentPathFinder implements com.radynamics.dallipay.cryptoledger.PaymentPathFinder {
    public PaymentPath[] find(CurrencyConverter currencyConverter, Payment p) {
        if (p == null) throw new IllegalArgumentException("Parameter 'p' cannot be null");

        var list = new ArrayList<PaymentPath>();

        var ledgerCcy = new Currency(p.getLedger().getNativeCcySymbol());
        list.add(new LedgerNativeCcyPath(currencyConverter, ledgerCcy));

        if (p.getUserCcy().equals(ledgerCcy)) {
            return list.toArray(new PaymentPath[0]);
        }

        var senderValid = WalletValidator.isValidFormat(p.getLedger(), p.getSenderWallet());
        var receiverValid = WalletValidator.isValidFormat(p.getLedger(), p.getReceiverWallet());
        if (!senderValid || !receiverValid) {
            return list.toArray(new PaymentPath[0]);
        }

        var cf = new CurrencyFormatter(p.getLedger().getInfoProvider());
        {
            var candidates = new ArrayList<IssuedCurrencyPath>();
            var ccysBothAccepting = findCcysBothAccepting(p.getSenderWallet(), p.getReceiverWallet(), p.getUserCcy());
            for (var i = 0; i < ccysBothAccepting.length; i++) {
                var ccy = ccysBothAccepting[i];
                candidates.add(new IssuedCurrencyPath(cf, ccy, ccy.getTransferFee()));
            }
            candidates.sort(Comparator.comparing(IssuedCurrencyPath::getRank).reversed());
            list.addAll(candidates);
        }

        if (p.getSubmitter().supportsPathFinding()) {
            var candidates = new ArrayList<PathFindingPath>();
            var acceptedUserCcyByReceiver = Arrays.stream(p.getReceiverWallet().getBalances().all())
                    // Always compare without issued due it's missing after entered by user
                    .filter(o -> o.getCcy().withoutIssuer().equals(p.getUserCcy().withoutIssuer()))
                    .filter(o -> list.stream().noneMatch(x -> x.getCcy().equals(o.getCcy())))
                    .map(Money::getCcy)
                    .toArray(Currency[]::new);
            for (var ccy : acceptedUserCcyByReceiver) {
                var ledger = (com.radynamics.dallipay.cryptoledger.xrpl.Ledger) p.getLedger();
                if (ledger.existsPath(p.getSenderWallet(), p.getReceiverWallet(), Money.of(p.getAmount(), ccy))) {
                    candidates.add(new PathFindingPath(cf, ccy, ccy.getTransferFee()));
                }
            }
            candidates.sort(Comparator.comparing(PathFindingPath::getRank).reversed());
            list.addAll(candidates);
        }

        return list.toArray(new PaymentPath[0]);
    }

    private static Currency[] findCcysBothAccepting(Wallet senderWallet, Wallet receiverWallet, Currency userCcy) {
        var ccyAnyIssuerSender = findSameCode(senderWallet.getBalances(), userCcy);
        // Use ledger native currency if expected currency code isn't available
        if (ccyAnyIssuerSender == null) {
            return new Currency[0];
        }

        return findEqual(senderWallet.getBalances(), receiverWallet.getBalances(), ccyAnyIssuerSender);
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
