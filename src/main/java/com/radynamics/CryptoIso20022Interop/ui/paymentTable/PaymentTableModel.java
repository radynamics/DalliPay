package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Validator;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;

public class PaymentTableModel extends AbstractTableModel {
    private final String[] columnNames = {COL_OBJECT, COL_VALIDATION_RESULTS, COL_SELECTOR, COL_STATUS, COL_RECEIVER_ISO20022, COL_RECEIVER_LEDGER, COL_AMOUNT, COL_CCY, COL_TRX_STATUS, COL_DETAIL};
    private Object[][] data;
    private final TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;
    private Actor showWalletOf = Actor.Receiver;

    public static final String COL_OBJECT = "object";
    public static final String COL_VALIDATION_RESULTS = "validationResults";
    public static final String COL_SELECTOR = "selector";
    public static final String COL_STATUS = "status";
    public static final String COL_RECEIVER_ISO20022 = "receiverIso20022";
    public static final String COL_RECEIVER_LEDGER = "receiverLedger";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_CCY = "ccy";
    public static final String COL_TRX_STATUS = "transmissionStatus";
    public static final String COL_DETAIL = "detail";

    public PaymentTableModel(TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;
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
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return col == getColumnIndex(COL_RECEIVER_LEDGER);
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public void load(Transaction[] data) {
        ArrayList<Object[]> list = new ArrayList<>();
        for (var t : data) {
            var ccy = transformInstruction.getTargetCcy();
            var amt = currencyConverter.convert(t.getLedger().convertToNativeCcyAmount(t.getAmountSmallestUnit()), t.getCcy(), ccy);
            var validationResults = validate(t);
            Object actorAddressOrAccount = getActorAddressOrAccount(t);
            Object actorLedger = getActorWalletText(t);
            var highestStatus = getHighestStatus(validationResults);
            list.add(new Object[]{t, validationResults, isSelected(highestStatus), highestStatus, actorAddressOrAccount, actorLedger, amt, ccy, t.getTransmission(), "detail..."});
        }

        this.data = list.toArray(new Object[0][0]);
    }

    private Object getActorAddressOrAccount(Transaction t) {
        Object actorAddressOrAccount = showWalletOf.get(t.getSenderAddress(), t.getReceiverAddress());
        if (actorAddressOrAccount == null) {
            var actorAccount = showWalletOf.get(t.getSenderAccount(), t.getReceiverAccount());
            actorAddressOrAccount = actorAccount == null ? IbanAccount.Empty : actorAccount;
        }
        return actorAddressOrAccount;
    }

    private String getActorWalletText(Transaction t) {
        var wallet = showWalletOf.get(t.getSenderWallet(), t.getReceiverWallet());
        return wallet == null ? "" : wallet.getPublicKey();
    }

    private ValidationResult[] validate(Transaction t) {
        return new Validator().validate(t);
    }

    private ValidationState getHighestStatus(ValidationResult[] results) {
        var highest = ValidationState.Ok;
        for (var r : results) {
            highest = r.getStatus().higherThan(highest) ? r.getStatus() : highest;
        }
        return highest;
    }

    private boolean isSelected(ValidationState highestStatus) {
        return highestStatus != ValidationState.Error;
    }

    public Transaction[] selectedPayments() {
        var list = new ArrayList<Transaction>();
        for (var item : this.data) {
            if ((boolean) item[getColumnIndex(COL_SELECTOR)]) {
                list.add((Transaction) item[0]);
            }
        }
        return list.toArray(new Transaction[0]);
    }

    public void onTransactionChanged(int row, Transaction t) {
        var validationResults = validate(t);
        var highestStatus = getHighestStatus(validationResults);

        setValueAt(validationResults, row, getColumnIndex(COL_VALIDATION_RESULTS));
        setValueAt(highestStatus, row, getColumnIndex(COL_STATUS));
        setValueAt(isSelected(highestStatus), row, getColumnIndex(COL_SELECTOR));
        setValueAt(t.getTransmission(), row, getColumnIndex(COL_TRX_STATUS));
    }

    public Actor getShowWalletOf() {
        return showWalletOf;
    }

    public void setShowWalletOf(Actor actor) {
        this.showWalletOf = actor;
    }
}
