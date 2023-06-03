package com.radynamics.dallipay.cryptoledger.generic.paymentpath;

import com.radynamics.dallipay.cryptoledger.LedgerNativeCcyPath;
import com.radynamics.dallipay.cryptoledger.PaymentPath;
import com.radynamics.dallipay.cryptoledger.WalletValidator;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.iso20022.Payment;

import java.util.ArrayList;
import java.util.List;

public class PaymentPathFinder implements com.radynamics.dallipay.cryptoledger.PaymentPathFinder {
    private final com.radynamics.dallipay.cryptoledger.PaymentPathFinder enhancedFinder;

    public PaymentPathFinder(com.radynamics.dallipay.cryptoledger.PaymentPathFinder enhancedFinder) {
        this.enhancedFinder = enhancedFinder;
    }

    @Override
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

        if (enhancedFinder != null) {
            list.addAll(List.of(enhancedFinder.find(currencyConverter, p)));
        }

        return list.toArray(new PaymentPath[0]);
    }
}
