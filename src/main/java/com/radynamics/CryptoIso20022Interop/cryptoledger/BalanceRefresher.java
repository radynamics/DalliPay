package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.util.Hashtable;
import java.util.function.Function;

public class BalanceRefresher {
    public void refreshAllSenderWallets(Payment[] payments) {
        refresh(payments, (Payment::getSenderWallet));
    }

    public void refresh(Payment[] payments) {
        refresh(payments, (Payment::getSenderWallet));
        refresh(payments, (Payment::getReceiverWallet));
    }

    private void refresh(Payment[] payments, Function<Payment, Wallet> getWallet) {
        var refreshed = new Hashtable<String, MoneyBag>();
        for (var p : payments) {
            var wallet = getWallet.apply(p);
            if (wallet == null) {
                continue;
            }

            // Use cached balance for other instances of the same wallet.
            if (refreshed.containsKey(wallet.getPublicKey())) {
                wallet.getBalances().replaceBy(refreshed.get(wallet.getPublicKey()));
                continue;
            }

            refresh(p.getLedger(), wallet);
            if (wallet.getBalances().isEmpty()) {
                continue;
            }
            refreshed.put(wallet.getPublicKey(), wallet.getBalances());
        }
    }

    public void refresh(Ledger ledger, Wallet wallet) {
        ledger.refreshBalance(wallet);
    }
}
