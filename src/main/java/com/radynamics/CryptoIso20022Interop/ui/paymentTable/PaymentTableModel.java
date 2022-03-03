package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.AsyncWalletInfoLoader;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.exchange.HistoricExchangeRateLoader;
import com.radynamics.CryptoIso20022Interop.iso20022.AsyncValidator;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class PaymentTableModel extends AbstractTableModel {
    private final String[] columnNames = {COL_OBJECT, COL_VALIDATION_RESULTS, COL_SELECTOR, COL_STATUS, COL_SENDER_LEDGER, COL_RECEIVER_ISO20022, COL_RECEIVER_LEDGER,
            COL_BOOKED, COL_AMOUNT, COL_CCY, COL_TRX_STATUS, COL_DETAIL};
    private Object[][] data = new Object[0][];
    private final HistoricExchangeRateLoader exchangeRateLoader;
    private PaymentValidator validator;
    private Actor actor = Actor.Sender;
    private ArrayList<ProgressListener> listener = new ArrayList<>();

    public static final String COL_OBJECT = "object";
    public static final String COL_VALIDATION_RESULTS = "validationResults";
    public static final String COL_SELECTOR = "selector";
    public static final String COL_STATUS = "status";
    public static final String COL_SENDER_LEDGER = "senderLedger";
    public static final String COL_RECEIVER_ISO20022 = "receiverIso20022";
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
        return data == null ? 0 : data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    private int getColumnIndex(String identifier) {
        return Arrays.asList(columnNames).indexOf(identifier);
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        if (col == getColumnIndex(COL_SELECTOR)) {
            var validationResults = (ValidationResult[]) getValueAt(row, getColumnIndex(COL_VALIDATION_RESULTS));
            return isSelectable(getHighestStatus(validationResults));
        }
        if (actor == Actor.Receiver) {
            return col == getColumnIndex(COL_RECEIVER_ISO20022);
        }
        if (actor == Actor.Sender) {
            return col == getColumnIndex(COL_SENDER_LEDGER) || col == getColumnIndex(COL_RECEIVER_LEDGER);
        }
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public void load(Payment[] data) {
        ArrayList<Object[]> list = new ArrayList<>();
        for (var t : data) {
            var amount = actor == Actor.Sender ? t.getAmount() : null;
            list.add(new Object[]{t, new ValidationResult[0], true, null, null, null, null, t.getBooked(), amount, t.getFiatCcy(), t.getTransmission(), "detail..."});
        }

        this.data = list.toArray(new Object[0][0]);
        var row = 0;
        for (var t : data) {
            setAccountAndWallet(t, row);
            row++;
        }
        fireTableDataChanged();

        loadAsync(data);
    }

    private void loadAsync(Payment[] data) {
        if (data.length == 0) {
            raiseProgress(new Progress(0, 0));
            return;
        }

        var queue = new ConcurrentLinkedQueue<CompletableFuture<Payment>>();
        for (var p : data) {
            var future = loadAsync(p);
            future.thenAccept((result) -> {
                queue.remove(future);
                var total = data.length;
                var loaded = total - queue.size();
                raiseProgress(new Progress(loaded, total));
            });
            queue.add(future);
        }
    }

    private CompletableFuture<Payment> loadAsync(Payment p) {
        var l = new AsyncWalletInfoLoader();
        var loadWalletInfo = l.load(p).thenAccept(result -> {
            var rowIndex = getRowIndex(result.getPayment());

            var senderCellValue = new WalletCellValue(result.getPayment().getSenderWallet(), result.getSenderInfo());
            setValueAt(senderCellValue, rowIndex, getColumnIndex(COL_SENDER_LEDGER));
            var receiverCellValue = new WalletCellValue(result.getPayment().getReceiverWallet(), result.getReceiverInfo());
            setValueAt(receiverCellValue, rowIndex, getColumnIndex(COL_RECEIVER_LEDGER));
        });
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

    private Object getActorAddressOrAccount(Payment t) {
        Object actorAddressOrAccount = actor.get(t.getReceiverAddress(), t.getSenderAddress());
        if (actorAddressOrAccount == null) {
            var actorAccount = actor.get(t.getReceiverAccount(), t.getSenderAccount());
            actorAddressOrAccount = actorAccount == null ? IbanAccount.Empty : actorAccount;
        }
        return actorAddressOrAccount;
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
            if ((boolean) item[getColumnIndex(COL_SELECTOR)]) {
                list.add((Payment) item[0]);
            }
        }
        return list.toArray(new Payment[0]);
    }

    public void onTransactionChanged(Payment t) {
        setAccountAndWallet(t, getRowIndex(t));
        validateAsync(t);

        setValueAt(t.getTransmission(), getRowIndex(t), getColumnIndex(COL_TRX_STATUS));
    }

    private void setAccountAndWallet(Payment t, int row) {
        setValueAt(new WalletCellValue(t.getSenderWallet()), row, getColumnIndex(COL_SENDER_LEDGER));
        setValueAt(getActorAddressOrAccount(t), row, getColumnIndex(COL_RECEIVER_ISO20022));
        setValueAt(new WalletCellValue(t.getReceiverWallet()), row, getColumnIndex(COL_RECEIVER_LEDGER));
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
