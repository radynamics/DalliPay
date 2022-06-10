package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.AsyncWalletInfoLoader;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.exchange.HistoricExchangeRateLoader;
import com.radynamics.CryptoIso20022Interop.iso20022.*;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class PaymentTableModel extends AbstractTableModel {
    private final String[] columnNames = {COL_OBJECT, COL_VALIDATION_RESULTS, COL_SELECTOR, COL_STATUS, COL_SENDER_LEDGER, COL_SENDER_ACCOUNT, COL_RECEIVER_ACCOUNT, COL_RECEIVER_LEDGER,
            COL_BOOKED, COL_AMOUNT, COL_CCY, COL_TRX_STATUS, COL_DETAIL};
    private Record[] data = new Record[0];
    private final HistoricExchangeRateLoader exchangeRateLoader;
    private PaymentValidator validator;
    private Actor actor = Actor.Sender;
    private ArrayList<ProgressListener> listener = new ArrayList<>();
    private final AsyncWalletInfoLoader walletInfoLoader = new AsyncWalletInfoLoader();

    public static final String COL_OBJECT = "object";
    public static final String COL_VALIDATION_RESULTS = "validationResults";
    public static final String COL_SELECTOR = "selector";
    public static final String COL_STATUS = "status";
    public static final String COL_SENDER_LEDGER = "senderLedger";
    public static final String COL_SENDER_ACCOUNT = "senderAccount";
    public static final String COL_RECEIVER_ACCOUNT = "receiverAccount";
    public static final String COL_RECEIVER_LEDGER = "receiverLedger";
    public static final String COL_BOOKED = "valuta";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_CCY = "ccy";
    public static final String COL_TRX_STATUS = "transmissionStatus";
    public static final String COL_DETAIL = "detail";

    public PaymentTableModel(HistoricExchangeRateLoader exchangeRateLoader, PaymentValidator validator) {
        this.exchangeRateLoader = exchangeRateLoader;
        this.validator = validator;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    private int getColumnIndex(String identifier) {
        return Arrays.asList(columnNames).indexOf(identifier);
    }

    public Object getValueAt(int row, int col) {
        var item = data[row];
        if (getColumnIndex(COL_OBJECT) == col) {
            return item.payment;
        } else if (getColumnIndex(COL_VALIDATION_RESULTS) == col) {
            return item.validationResults;
        } else if (getColumnIndex(COL_SELECTOR) == col) {
            return item.selected;
        } else if (getColumnIndex(COL_STATUS) == col) {
            return item.status;
        } else if (getColumnIndex(COL_SENDER_LEDGER) == col) {
            return item.getSenderLedger();
        } else if (getColumnIndex(COL_SENDER_ACCOUNT) == col) {
            return item.getActorAddressOrAccount(Actor.Sender);
        } else if (getColumnIndex(COL_RECEIVER_ACCOUNT) == col) {
            return item.getActorAddressOrAccount(Actor.Receiver);
        } else if (getColumnIndex(COL_RECEIVER_LEDGER) == col) {
            return item.getReceiverLedger();
        } else if (getColumnIndex(COL_BOOKED) == col) {
            return item.payment.getBooked();
        } else if (getColumnIndex(COL_AMOUNT) == col) {
            return item.getAmount(actor);
        } else if (getColumnIndex(COL_CCY) == col) {
            return item.payment.getFiatCcy();
        } else if (getColumnIndex(COL_TRX_STATUS) == col) {
            return item.payment.getTransmission();
        } else if (getColumnIndex(COL_DETAIL) == col) {
            return "detail...";
        }
        return null;
    }

    public void setValueAt(Object value, int row, int col) {
        var item = data[row];
        if (getColumnIndex(COL_VALIDATION_RESULTS) == col) {
            item.validationResults = (ValidationResult[]) value;
        } else if (getColumnIndex(COL_SELECTOR) == col) {
            item.selected = (boolean) value;
        } else if (getColumnIndex(COL_STATUS) == col) {
            item.status = (ValidationState) value;
        } else if (getColumnIndex(COL_SENDER_LEDGER) == col) {
            if (value instanceof WalletCellValue) {
                item.setSenderLedger((WalletCellValue) value);
            } else {
                item.setSenderLedger((String) value);
            }
        } else if (getColumnIndex(COL_SENDER_ACCOUNT) == col) {
            item.payment.setSenderAccount(createAccountOrNull((String) value, item.payment.getSenderWallet()));
        } else if (getColumnIndex(COL_RECEIVER_ACCOUNT) == col) {
            item.payment.setReceiverAccount(createAccountOrNull((String) value, item.payment.getReceiverWallet()));
        } else if (getColumnIndex(COL_RECEIVER_LEDGER) == col) {
            if (value instanceof WalletCellValue) {
                item.setReceiverLedger((WalletCellValue) value);
            } else {
                item.setReceiverLedger((String) value);
            }
        } else if (getColumnIndex(COL_AMOUNT) == col) {
            item.setAmount((Double) value);
        } else if (getColumnIndex(COL_CCY) == col) {
            item.payment.setFiatCcy((String) value);
        } else {
            throw new NotImplementedException(String.format("Setting value for column %s is not implemented", col));
        }
        fireTableCellUpdated(row, col);
    }

    private Account createAccountOrNull(String text, Wallet wallet) {
        if (!StringUtils.isEmpty(text)) {
            return AccountFactory.create(text);
        }
        if (wallet != null && !StringUtils.isEmpty(wallet.getPublicKey())) {
            return AccountFactory.create(wallet.getPublicKey());
        }
        return null;
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        if (col == getColumnIndex(COL_SELECTOR)) {
            return isSelectable(getHighestStatus(getValidationResults(row)));
        }
        if (actor == Actor.Receiver) {
            return col == getColumnIndex(COL_SENDER_ACCOUNT) || col == getColumnIndex(COL_RECEIVER_ACCOUNT);
        }
        if (actor == Actor.Sender) {
            return col == getColumnIndex(COL_SENDER_LEDGER) || col == getColumnIndex(COL_RECEIVER_LEDGER);
        }
        return false;
    }

    public void load(Record[] data) {
        if (data == null) throw new IllegalArgumentException("Parameter 'data' cannot be null");
        this.data = data;
        fireTableDataChanged();

        loadAsync();
    }

    private void loadAsync() {
        if (data.length == 0) {
            raiseProgress(new Progress(0, 0));
            return;
        }

        var queue = new ConcurrentLinkedQueue<CompletableFuture<Payment>>();
        for (var p : data) {
            var future = loadAsync(p.payment);
            future.thenAccept((result) -> {
                synchronized (this) {
                    queue.remove(future);
                    var total = data.length;
                    var loaded = total - queue.size();
                    raiseProgress(new Progress(loaded, total));
                }
            });
            queue.add(future);
        }
    }

    private CompletableFuture<Payment> loadAsync(Payment p) {
        var loadWalletInfo = loadWalletInfoAsync(p);
        var loadExchangeRate = new CompletableFuture<Void>();
        if (actor == Actor.Receiver) {
            loadExchangeRate = exchangeRateLoader.loadAsync(p).thenAccept(t -> {
                setValueAt(t.getAmount(), getRowIndex(t), getColumnIndex(COL_AMOUNT));
                setValueAt(t.getFiatCcy(), getRowIndex(t), getColumnIndex(COL_CCY));
            });
        } else {
            loadExchangeRate.complete(null);
        }

        var future = new CompletableFuture<Payment>();
        var finalLoadExchangeRate = loadExchangeRate;
        Executors.newCachedThreadPool().submit(() -> {
            CompletableFuture.allOf(loadWalletInfo, finalLoadExchangeRate).join();
            // Validation can start after loadExchangeRate completed.
            validateAsync(p).thenAccept((result) -> {
                future.complete(p);
            });
        });
        return future;
    }

    private CompletableFuture<Void> loadWalletInfoAsync(Payment p) {
        return walletInfoLoader.load(p).thenAccept(result -> {
            var rowIndex = getRowIndex(result.getPayment());

            var senderCellValue = new WalletCellValue(result.getPayment().getSenderWallet(), result.getSenderInfo());
            setValueAt(senderCellValue, rowIndex, getColumnIndex(COL_SENDER_LEDGER));
            var receiverCellValue = new WalletCellValue(result.getPayment().getReceiverWallet(), result.getReceiverInfo());
            setValueAt(receiverCellValue, rowIndex, getColumnIndex(COL_RECEIVER_LEDGER));
        });
    }

    private CompletableFuture<Void> validateAsync(Payment payment) {
        var av = new AsyncValidator(validator);
        return av.validate(payment).thenAccept(result -> {
            var rowIndex = getRowIndex(result.left);
            var validationResults = result.right;

            setValueAt(validationResults, rowIndex, getColumnIndex(COL_VALIDATION_RESULTS));
            var highestStatus = getHighestStatus(validationResults);
            setValueAt(isSelected(result.left, highestStatus), rowIndex, getColumnIndex(COL_SELECTOR));
            setValueAt(highestStatus, rowIndex, getColumnIndex(COL_STATUS));
        });
    }

    private ValidationState getHighestStatus(ValidationResult[] results) {
        var highest = ValidationState.Ok;
        for (var r : results) {
            highest = r.getStatus().higherThan(highest) ? r.getStatus() : highest;
        }
        return highest;
    }

    private boolean isSelected(Payment p, ValidationState highestStatus) {
        var selected = true;
        if (actor == Actor.Sender) {
            selected = p.getTransmission() == TransmissionState.Pending;
        }
        return selected && isSelectable(highestStatus);
    }

    private boolean isSelectable(ValidationState highestStatus) {
        return highestStatus != ValidationState.Error;
    }

    public Payment[] selectedPayments() {
        var list = new ArrayList<Payment>();
        for (var item : this.data) {
            if (item.selected) {
                list.add(item.payment);
            }
        }
        return list.toArray(new Payment[0]);
    }

    public ValidationResult[] getValidationResults(Payment[] payments) {
        var list = new ArrayList<ValidationResult>();
        for (var p : payments) {
            list.addAll(Arrays.asList(getValidationResults(p)));
        }
        return list.toArray(new ValidationResult[0]);
    }

    private ValidationResult[] getValidationResults(Payment p) {
        return getValidationResults(getRowIndex(p));
    }

    private ValidationResult[] getValidationResults(int row) {
        return (ValidationResult[]) getValueAt(row, getColumnIndex(COL_VALIDATION_RESULTS));
    }

    public void onAccountOrWalletsChanged(Payment t) {
        Executors.newCachedThreadPool().submit(() -> {
            loadWalletInfoAsync(t).thenAccept((result) -> onTransactionChanged(t));
        });
    }

    public void onTransactionChanged(Payment t) {
        int row = getRowIndex(t);
        fireTableRowsUpdated(row, row);

        validateAsync(t);
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    private int getRowIndex(Payment t) {
        for (var i = 0; i < getRowCount(); i++) {
            var obj = (Payment) getValueAt(i, getColumnIndex(COL_OBJECT));
            if (obj.equals(t)) {
                return i;
            }
        }

        throw new RuntimeException(String.format("Could not find row index for %s", t.getId()));
    }

    public void addProgressListener(ProgressListener l) {
        listener.add(l);
    }

    private void raiseProgress(Progress progress) {
        for (var l : listener) {
            l.onProgress(progress);
        }
    }
}
