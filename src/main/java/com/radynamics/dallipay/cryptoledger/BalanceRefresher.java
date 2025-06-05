package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.iso20022.Payment;

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
        if (!ledger.createWalletValidator().isValidFormat(wallet)) {
            return;
        }

        // Use cached balance for other instances of the same wallet.
        var key = new WalletKey(wallet);
        if (cache.isPresent(key)) {
            wallet.getBalances().replaceBy(cache.get(key));
            return;
        }

        var walletInput = ledger.createWalletInput(wallet.getPublicKey());
        wallet.getBalances().replaceBy(ledger.getBalance(walletInput, useCache));
        if (wallet.getBalances().isEmpty()) {
            return;
        }
        cache.add(key, wallet.getBalances());
    }
}
