package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.util.Hashtable;
import java.util.function.Function;

public class BalanceRefresher {
    private final Hashtable<String, MoneyBag> cache = new Hashtable<>();

    public void refreshAllSenderWallets(Payment[] payments) {
        refresh(payments, (Payment::getSenderWallet));
    }

    public void refresh(Payment[] payments) {
        refresh(payments, (Payment::getSenderWallet));
        refresh(payments, (Payment::getReceiverWallet));
    }

    private void refresh(Payment[] payments, Function<Payment, Wallet> getWallet) {
        for (var p : payments) {
            refresh(p.getLedger(), getWallet.apply(p));
        }
    }

    public void refresh(Ledger ledger, Wallet wallet) {
        loadOrGet(ledger, wallet);
    }

    private void loadOrGet(Ledger ledger, Wallet wallet) {
        if (!WalletValidator.isValidFormat(ledger, wallet)) {
            return;
        }

        var key = wallet.getPublicKey();
        // Use cached balance for other instances of the same wallet.
        if (cache.containsKey(key)) {
            wallet.getBalances().replaceBy(cache.get(key));
            return;
        }

        ledger.refreshBalance(wallet, false);
        if (wallet.getBalances().isEmpty()) {
            return;
        }
        cache.put(key, wallet.getBalances());
    }
}
