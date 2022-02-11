package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.google.common.primitives.UnsignedLong;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.util.Hashtable;

public class BalanceRefresher {
    public void refreshAllSenderWallets(Payment[] payments) {
        var refreshed = new Hashtable<String, UnsignedLong>();
        for (var p : payments) {
            var wallet = p.getSenderWallet();
            if (wallet == null) {
                continue;
            }

            // Use cached balance for other instances of the same wallet.
            if (refreshed.containsKey(wallet.getPublicKey())) {
                wallet.setLedgerBalance(refreshed.get(wallet.getPublicKey()));
                continue;
            }

            p.getLedger().refreshBalance(wallet);
            if (wallet.getLedgerBalanceSmallestUnit() == null) {
                continue;
            }
            refreshed.put(wallet.getPublicKey(), wallet.getLedgerBalanceSmallestUnit());
        }
    }
}
