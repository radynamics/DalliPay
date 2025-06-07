package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.cryptoledger.AsyncWalletInfoLoader;
import com.radynamics.dallipay.cryptoledger.BalanceRefresher;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.exchange.HistoricExchangeRateLoader;
import com.radynamics.dallipay.iso20022.AsyncValidator;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.PaymentValidator;
import com.radynamics.dallipay.transformation.TransactionTranslator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class DataLoader {
    private final static Logger log = LogManager.getLogger(DataLoader.class);

    private final PaymentTableModel model;
    private HistoricExchangeRateLoader exchangeRateLoader;
    private PaymentValidator validator;
    private TransactionTranslator transactionTranslator;
    private final AsyncWalletInfoLoader walletInfoLoader = new AsyncWalletInfoLoader();
    private final ArrayList<ProgressListener> progressListener = new ArrayList<>();
    private final ArrayList<Record> payments = new ArrayList<>();
    private final ConcurrentLinkedQueue<CompletableFuture<Payment>> queue = new ConcurrentLinkedQueue<>();

    public DataLoader(PaymentTableModel model) {
        this.model = model;
    }

    public void loadAsync(Record[] payments) {
        this.payments.clear();
        this.payments.addAll(List.of(payments));
        if (payments.length == 0) {
            raiseProgress(new Progress(0, 0));
            return;
        }

        validator.clearCache();
        validator.getHistoryValidator().clearCache();
        load(payments);
    }

    public void loadAsync(Record p) {
        payments.add(p);
        load(new Record[]{p});
    }

    private void load(Record[] payments) {
        raiseProgress(new Progress(0, payments.length));
        var br = new BalanceRefresher(payments[0].payment.getLedger().getNetwork());
        for (var p : payments) {
            var future = loadAsync(p, br);
            future.thenAccept((result) -> {
                removeFromQueue(payments, future);
            }).exceptionally((t) -> {
                // Eg. "FeignException$TooManyRequests" (Http 429 from xrplcluster.com).
                log.error(t.getMessage(), t);

                // Ensure UI doesn't get stuck at "Validating...".
                model.setValidationResults(p.payment, new ValidationResult[]{ValidationResult.of(t)});
                // Ensure UI doesn't get stuck at 'x / y loaded...".
                removeFromQueue(payments, future);
                return null;
            });
            queue.add(future);
        }
    }

    private synchronized void removeFromQueue(Record[] payments, CompletableFuture<Payment> future) {
        queue.remove(future);
        var total = payments.length;
        var loaded = total - queue.size();
        raiseProgress(new Progress(loaded, total));
    }

    private CompletableFuture<Payment> loadAsync(Record item, BalanceRefresher br) {
        var p = item.payment;
        var loadBalancesAndHistory = CompletableFuture.runAsync(() -> {
            // When fetching received payments following data is not needed and shouldn't be loaded for better performance.
            if (model.getActor() != Actor.Sender) {
                return;
            }
            br.refresh(p);

            // Available payment paths are loaded inside. To ensure caching works call api sequentially.
            synchronized (this) {
                // When fetching received payments transaction ccy must not be adjusted based on user ccy (pain.001).
                transactionTranslator.applyUserCcy(p);
            }
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
            try {
                CompletableFuture.allOf(loadBalancesAndHistory).join();
                CompletableFuture.allOf(loadWalletInfo, finalLoadExchangeRate).join();
                // Validation can start after loadExchangeRate completed.
                validateAsync(item).thenAccept((result) -> future.complete(p));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    private CompletableFuture<Void> loadWalletInfoAsync(Record item) {
        return walletInfoLoader.load(item.payment).thenAccept(result -> {
            var senderCellValue = new WalletCellValue(item.payment.getSenderWallet(), null, result.getSenderInfos());
            // Don't use setValueAt to prevent event raising during async load.
            item.setSenderLedger(senderCellValue);
            var receiverCellValue = new WalletCellValue(item.payment.getReceiverWallet(), item.payment.getDestinationTag(), result.getReceiverInfos());
            item.setReceiverLedger(receiverCellValue);
        });
    }

    private CompletableFuture<Void> validateAsync(Record item) {
        var av = new AsyncValidator(validator);
        return av.validate(item.payment).thenAccept(result -> model.setValidationResults(item.payment, result.right));
    }

    public void onAccountOrWalletsChanged(Payment t) {
        Executors.newCachedThreadPool().submit(() -> {
            onAccountOrWalletsChangedAsync(t);
        });
    }

    public CompletableFuture<Void> onAccountOrWalletsChangedAsync(Payment t) {
        return loadWalletInfoAsync(getRecord(t).orElseThrow()).thenAccept((result) -> onTransactionChanged(t));
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

    public void init(HistoricExchangeRateLoader exchangeRateLoader, PaymentValidator validator, TransactionTranslator transactionTranslator) {
        this.exchangeRateLoader = exchangeRateLoader;
        this.validator = validator;
        this.transactionTranslator = transactionTranslator;
    }

    public boolean isLoading() {
        return !queue.isEmpty();
    }
}
