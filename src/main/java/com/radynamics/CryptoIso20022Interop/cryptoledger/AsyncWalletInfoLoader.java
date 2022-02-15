package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AsyncWalletInfoLoader {
    public CompletableFuture<PaymentWalletInfo>[] load(Payment[] payments) {
        var list = new ArrayList<CompletableFuture<PaymentWalletInfo>>();
        for (var p : payments) {
            list.add(load(p));
        }
        return list.toArray((CompletableFuture<PaymentWalletInfo>[]) Array.newInstance(CompletableFuture.class, list.size()));
    }

    public CompletableFuture<PaymentWalletInfo> load(Payment p) {
        var completableFuture = new CompletableFuture<PaymentWalletInfo>();

        Executors.newCachedThreadPool().submit(() -> {
            var aggregator = new WalletInfoAggregator(p.getLedger().getInfoProvider());
            completableFuture.complete(
                    new PaymentWalletInfo(p, aggregator.getMostImportant(p.getSenderWallet()), aggregator.getMostImportant(p.getReceiverWallet())))
            ;
        });

        return completableFuture;
    }

}
