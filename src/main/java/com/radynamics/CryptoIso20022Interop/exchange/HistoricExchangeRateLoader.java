package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class HistoricExchangeRateLoader {
    final static Logger log = LogManager.getLogger(HistoricExchangeRateLoader.class);
    private final TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;

    public HistoricExchangeRateLoader(TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;
    }

    public CompletableFuture<Payment>[] loadAsync(Payment[] payments) {
        var list = new ArrayList<CompletableFuture<Payment>>();
        for (var t : payments) {
            list.add(loadAsync(t));
        }
        return list.toArray((CompletableFuture<Payment>[]) Array.newInstance(CompletableFuture.class, list.size()));
    }

    public CompletableFuture<Payment> loadAsync(Payment t) {
        var completableFuture = new CompletableFuture<Payment>();

        Executors.newCachedThreadPool().submit(() -> {
            var ccyPair = t.createCcyPair();
            if (ccyPair.isOneToOne()) {
                t.setExchangeRate(ExchangeRate.OneToOne(ccyPair));
                completableFuture.complete(t);
                return;
            }

            var source = transformInstruction.getHistoricExchangeRateSource();
            var rate = CurrencyPair.contains(source.getSupportedPairs(), ccyPair) ? source.rateAt(ccyPair, t.getBooked()) : null;
            if (rate == null) {
                log.info(String.format("No FX rate found for %s at %s with %s", ccyPair.getDisplayText(), t.getBooked(), source.getDisplayText()));
                t.setAmountUnknown();
                completableFuture.complete(t);
                return;
            }
            var cc = new CurrencyConverter(new ExchangeRate[]{rate});
            t.setExchangeRate(cc.get(ccyPair));
            completableFuture.complete(t);
        });

        return completableFuture;
    }
}
