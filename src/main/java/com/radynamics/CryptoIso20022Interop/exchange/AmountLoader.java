package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.ui.paymentTable.Actor;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AmountLoader {
    private final TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;

    private Actor actor;

    public AmountLoader(TransformInstruction transformInstruction, CurrencyConverter currencyConverter, Actor actor) {
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;
        this.actor = actor;
    }

    public CompletableFuture<Payment>[] loadAsync(Payment[] payments) {
        var list = new ArrayList<CompletableFuture<Payment>>();
        for (var t : payments) {
            list.add(loadAsync(t));
        }
        return list.toArray((CompletableFuture<Payment>[]) Array.newInstance(CompletableFuture.class, list.size()));
    }

    private CompletableFuture<Payment> loadAsync(Payment t) {
        var completableFuture = new CompletableFuture<Payment>();

        Executors.newCachedThreadPool().submit(() -> {
            var ccy = transformInstruction.getTargetCcy();
            var ccyPair = new CurrencyPair(t.getLedgerCcy(), ccy);
            CurrencyConverter cc;
            if (actor == Actor.Sender) {
                currencyConverter.convert(t.getLedger().convertToNativeCcyAmount(t.getLedgerAmountSmallestUnit()), t.getLedgerCcy(), ccy);
                cc = currencyConverter;
            } else {
                var source = transformInstruction.getHistoricExchangeRateSource();
                var rate = source.rateAt(ccyPair, t.getBooked());
                if (rate == null) {
                    LogManager.getLogger().info(String.format("No FX rate found for %s at %s with %s", ccyPair.getDisplayText(), t.getBooked(), source.getDisplayText()));
                    t.setAmountUnknown();
                    completableFuture.complete(t);
                    return;
                }
                cc = new CurrencyConverter(new ExchangeRate[]{rate});
            }
            var amt = cc.convert(t.getLedger().convertToNativeCcyAmount(t.getLedgerAmountSmallestUnit()), t.getLedgerCcy(), ccy);
            t.setAmount(amt, ccy);
            completableFuture.complete(t);
        });

        return completableFuture;
    }
}
