package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.util.function.Function;

public class BalanceRefresher {
    private final Cache<MoneyBag> cache;

    public BalanceRefresher(NetworkInfo network) {
        cache = new Cache<>(network.getUrl().toString());
    }

    public void refreshAllSenderWallets(Payment[] payments) {
        refresh(payments, (Payment::getSenderWallet));
    }

    public void refresh(Payment payment) {
        refresh(new Payment[]{payment});
    }

    public void refresh(Payment[] payments) {
        refresh(payments, (Payment::getSenderWallet));
        refresh(payments, (Payment::getReceiverWallet));
    }

    private void refresh(Payment[] payments, Function<Payment, Wallet> getWallet) {
        for (var p : payments) {
            loadOrGet(p.getLedger(), getWallet.apply(p), true);
        }
    }

    public void refresh(Ledger ledger, Wallet wallet) {
        loadOrGet(ledger, wallet, false);
    }

    private synchronized void loadOrGet(Ledger ledger, Wallet wallet, boolean useCache) {
        if (!WalletValidator.isValidFormat(ledger, wallet)) {
            return;
        }

        // Use cached balance for other instances of the same wallet.
        if (cache.isPresent(wallet)) {
            wallet.getBalances().replaceBy(cache.get(wallet));
            return;
        }

        ledger.refreshBalance(wallet, useCache);
        if (wallet.getBalances().isEmpty()) {
            return;
        }
        cache.add(wallet, wallet.getBalances());
    }
}
