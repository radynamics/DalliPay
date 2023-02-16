package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.AsyncWalletInfoLoader;
import com.radynamics.CryptoIso20022Interop.cryptoledger.BalanceRefresher;
import com.radynamics.CryptoIso20022Interop.exchange.HistoricExchangeRateLoader;
import com.radynamics.CryptoIso20022Interop.iso20022.AsyncValidator;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator;
import com.radynamics.CryptoIso20022Interop.transformation.TransactionTranslator;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class DataLoader {
    private final PaymentTableModel model;
    private final HistoricExchangeRateLoader exchangeRateLoader;
    private final PaymentValidator validator;
    private final TransactionTranslator transactionTranslator;
    private final AsyncWalletInfoLoader walletInfoLoader = new AsyncWalletInfoLoader();
    private final ArrayList<ProgressListener> progressListener = new ArrayList<>();
    private Record[] payments = new Record[0];

    public DataLoader(PaymentTableModel model, HistoricExchangeRateLoader exchangeRateLoader, PaymentValidator validator, TransactionTranslator transactionTranslator) {
        this.model = model;
        this.exchangeRateLoader = exchangeRateLoader;
        this.validator = validator;
        this.transactionTranslator = transactionTranslator;
    }

    public void loadAsync(Record[] payments) {
        this.payments = payments;
        if (payments.length == 0) {
            raiseProgress(new Progress(0, 0));
            return;
        }

        validator.clearCache();
        validator.getHistoryValidator().clearCache();
        var br = new BalanceRefresher(payments[0].payment.getLedger().getNetwork());
        var queue = new ConcurrentLinkedQueue<CompletableFuture<Payment>>();
        for (var p : payments) {
            var future = loadAsync(p, br);
            future.thenAccept((result) -> {
                synchronized (this) {
                    queue.remove(future);
                    var total = payments.length;
                    var loaded = total - queue.size();
                    raiseProgress(new Progress(loaded, total));
                }
            });
            queue.add(future);
        }
    }

    private CompletableFuture<Payment> loadAsync(Record item, BalanceRefresher br) {
        var p = item.payment;
        var loadBalancesAndHistory = CompletableFuture.runAsync(() -> {
            // When fetching received payments following data is not needed and shouldn't be loaded for better performance.
            if (model.getActor() != Actor.Sender) {
                return;
            }
            br.refresh(p);

            // When fetching received payments transaction ccy must not be adjusted based on user ccy (pain.001).
            transactionTranslator.applyUserCcy(p);
            validator.getHistoryValidator().loadHistory(p.getLedger(), p.getSenderWallet());
        });

        var loadWalletInfo = loadWalletInfoAsync(item);
        var loadExchangeRate = new CompletableFuture<Void>();
        if (model.getActor() == Actor.Receiver) {
            loadExchangeRate = exchangeRateLoader.loadAsync(p).thenAccept(t -> model.onTransactionChanged(p));
        } else {
            loadExchangeRate.complete(null);
        }

        var future = new CompletableFuture<Payment>();
        var finalLoadExchangeRate = loadExchangeRate;
        Executors.newCachedThreadPool().submit(() -> {
            CompletableFuture.allOf(loadBalancesAndHistory).join();
            CompletableFuture.allOf(loadWalletInfo, finalLoadExchangeRate).join();
            // Validation can start after loadExchangeRate completed.
            validateAsync(item).thenAccept((result) -> future.complete(p));
        });
        return future;
    }

    private CompletableFuture<Void> loadWalletInfoAsync(Record item) {
        return walletInfoLoader.load(item.payment).thenAccept(result -> {
            var senderCellValue = new WalletCellValue(item.payment.getSenderWallet(), result.getSenderInfos());
            // Don't use setValueAt to prevent event raising during async load.
            item.setSenderLedger(senderCellValue);
            var receiverCellValue = new WalletCellValue(item.payment.getReceiverWallet(), result.getReceiverInfos());
            item.setReceiverLedger(receiverCellValue);
        });
    }

    private CompletableFuture<Void> validateAsync(Record item) {
        var av = new AsyncValidator(validator);
        return av.validate(item.payment).thenAccept(result -> model.setValidationResults(item.payment, result.right));
    }

    public void onAccountOrWalletsChanged(Payment t) {
        Executors.newCachedThreadPool().submit(() -> {
            loadWalletInfoAsync(getRecord(t).orElseThrow()).thenAccept((result) -> onTransactionChanged(t));
        });
    }

    private Optional<Record> getRecord(Payment payment) {
        for (var p : payments) {
            if (p.payment == payment) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public CompletableFuture<Void> onTransactionChanged(Payment t) {
        validator.clearCache();
        return validateAsync(getRecord(t).orElseThrow());
    }

    public void addProgressListener(ProgressListener l) {
        progressListener.add(l);
    }

    private void raiseProgress(Progress progress) {
        for (var l : progressListener) {
            l.onProgress(progress);
        }
    }
}
