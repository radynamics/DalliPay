package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AsyncValidator {
    private PaymentValidator validator;

    public AsyncValidator(PaymentValidator validator) {
        this.validator = validator;
    }

    public CompletableFuture<ImmutablePair<Payment, ValidationResult[]>>[] validate(Payment[] payments) {
        var list = new ArrayList<CompletableFuture<ImmutablePair<Payment, ValidationResult[]>>>();
        for (var t : payments) {
            list.add(validate(t));
        }
        return list.toArray((CompletableFuture<ImmutablePair<Payment, ValidationResult[]>>[]) Array.newInstance(CompletableFuture.class, list.size()));
    }

    public CompletableFuture<ImmutablePair<Payment, ValidationResult[]>> validate(Payment t) {
        var completableFuture = new CompletableFuture<ImmutablePair<Payment, ValidationResult[]>>();

        Executors.newCachedThreadPool().submit(() -> {
            completableFuture.complete(new ImmutablePair<>(t, validator.validate(t)));
        });

        return completableFuture;
    }
}
