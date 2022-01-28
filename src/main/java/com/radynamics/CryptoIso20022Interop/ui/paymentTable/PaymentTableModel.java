package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

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

public class PaymentTableModel extends AbstractTableModel {
    private final String[] columnNames = {COL_OBJECT, COL_VALIDATION_RESULTS, COL_SELECTOR, COL_STATUS, COL_SENDER_LEDGER, COL_RECEIVER_ISO20022, COL_RECEIVER_LEDGER,
            COL_BOOKED, COL_AMOUNT, COL_CCY, COL_TRX_STATUS, COL_DETAIL};
    private Object[][] data;
    private final HistoricExchangeRateLoader exchangeRateLoader;
    private PaymentValidator validator;
    private Actor actor = Actor.Sender;

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
        return (actor == Actor.Receiver && col == getColumnIndex(COL_RECEIVER_ISO20022))
                || (actor == Actor.Sender && col == getColumnIndex(COL_RECEIVER_LEDGER));
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public void load(Payment[] data) {
        ArrayList<Object[]> list = new ArrayList<>();
        for (var t : data) {
            Object actorAddressOrAccount = getActorAddressOrAccount(t);
            Object senderLedger = t.getSenderWallet() == null ? "" : t.getSenderWallet().getPublicKey();
            Object receiverLedger = t.getReceiverWallet() == null ? "" : t.getReceiverWallet().getPublicKey();
            var amount = actor == Actor.Sender ? t.getAmount() : null;
            list.add(new Object[]{t, new ValidationResult[0], true, null, senderLedger, actorAddressOrAccount, receiverLedger, t.getBooked(), amount, t.getFiatCcy(), t.getTransmission(), "detail..."});
        }

        this.data = list.toArray(new Object[0][0]);
        fireTableDataChanged();

        if (actor == Actor.Receiver) {
            Arrays.stream(exchangeRateLoader.loadAsync(data)).forEach(future -> {
                future.thenAccept(t -> {
                    setValueAt(t.getAmount(), getRowIndex(t), getColumnIndex(COL_AMOUNT));
                    setValueAt(t.getFiatCcy(), getRowIndex(t), getColumnIndex(COL_CCY));
                    validateAsync(new Payment[]{t});
                });
            });
        } else {
            validateAsync(data);
        }
    }

    private Object getActorAddressOrAccount(Payment t) {
        Object actorAddressOrAccount = actor.get(t.getReceiverAddress(), t.getSenderAddress());
        if (actorAddressOrAccount == null) {
            var actorAccount = actor.get(t.getReceiverAccount(), t.getSenderAccount());
            actorAddressOrAccount = actorAccount == null ? IbanAccount.Empty : actorAccount;
        }
        return actorAddressOrAccount;
    }

    private void validateAsync(Payment[] payments) {
        var av = new AsyncValidator(validator);
        Arrays.stream(av.validate(payments)).forEach(future -> {
            future.thenAccept(result -> {
                var rowIndex = getRowIndex(result.left);
                var validationResults = result.right;

                setValueAt(validationResults, rowIndex, getColumnIndex(COL_VALIDATION_RESULTS));
                var highestStatus = getHighestStatus(validationResults);
                setValueAt(isSelected(result.left, highestStatus), rowIndex, getColumnIndex(COL_SELECTOR));
                setValueAt(highestStatus, rowIndex, getColumnIndex(COL_STATUS));
            });
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
        return selected && highestStatus != ValidationState.Error;
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

    public void onTransactionChanged(int row, Payment t) {
        validateAsync(new Payment[]{t});

        setValueAt(t.getTransmission(), getRowIndex(t), getColumnIndex(COL_TRX_STATUS));
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
}
