package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.util.Hashtable;

public class BalanceRefresher {
    public void refreshSenderWallet(Payment payment) {
        refreshAllSenderWallets(new Payment[]{payment});
    }

    public void refreshAllSenderWallets(Payment[] payments) {
        var refreshed = new Hashtable<String, MoneyBag>();
        for (var p : payments) {
            var wallet = p.getSenderWallet();
            if (wallet == null) {
                continue;
            }

            // Use cached balance for other instances of the same wallet.
            if (refreshed.containsKey(wallet.getPublicKey())) {
                wallet.getBalances().replaceBy(refreshed.get(wallet.getPublicKey()));
                continue;
            }

            p.getLedger().refreshBalance(wallet);
            if (wallet.getBalances().isEmpty()) {
                continue;
            }
            refreshed.put(wallet.getPublicKey(), wallet.getBalances());
        }
    }
}
