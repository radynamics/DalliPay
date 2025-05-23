package com.radynamics.dallipay.cryptoledger.xrpl.paymentpath;

import com.radynamics.dallipay.cryptoledger.LedgerNativeCcyPath;
import com.radynamics.dallipay.cryptoledger.PaymentPath;
import com.radynamics.dallipay.cryptoledger.generic.paymentpath.BothHolding;
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

        var walletValidator = p.getLedger().createWalletValidator();
        var senderValid = walletValidator.isValidFormat(p.getSenderWallet());
        var receiverValid = walletValidator.isValidFormat(p.getReceiverWallet());
        if (!senderValid || !receiverValid) {
            return list.toArray(new PaymentPath[0]);
        }

        var cf = new CurrencyFormatter(p.getLedger().getInfoProvider());
        if (p.getSubmitter().supportIssuedTokens()) {
            var candidates = new ArrayList<IssuedCurrencyPath>();
            var ccysBothAccepting = BothHolding.list(p.getSenderWallet(), p.getReceiverWallet(), p.getUserCcy());
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
                    // Always compare without issuer due it's missing after entered by user
                    .filter(o -> o.getCcy().withoutIssuer().equals(p.getUserCcy().withoutIssuer()))
                    .filter(o -> list.stream().noneMatch(x -> x.getCcy().equals(o.getCcy())))
                    .map(Money::getCcy)
                    .toArray(Currency[]::new);
            for (var ccy : acceptedUserCcyByReceiver) {
                // Assume a path is available if sale offers are available for the payment amount.
                if (p.getLedger().existsSellOffer(Money.of(p.getAmount(), ccy))) {
                    candidates.add(new PathFindingPath(cf, ccy, ccy.getTransferFee()));
                }
            }
            candidates.sort(Comparator.comparing(PathFindingPath::getRank).reversed());
            list.addAll(candidates);
        }

        return list.toArray(new PaymentPath[0]);
    }
}
