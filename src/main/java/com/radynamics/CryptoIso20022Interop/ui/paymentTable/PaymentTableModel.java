package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Status;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Validator;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class PaymentTableModel extends AbstractTableModel {
    private final String[] columnNames = {COL_OBJECT, COL_SELECTOR, COL_STATUS, COL_RECEIVER_ISO20022, COL_RECEIVER_LEDGER, COL_AMOUNT, COL_CCY, COL_DETAIL};
    private Object[][] data;
    private final TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;

    public static final String COL_OBJECT = "object";
    public static final String COL_SELECTOR = "selector";
    public static final String COL_STATUS = "status";
    public static final String COL_RECEIVER_ISO20022 = "receiverIso20022";
    public static final String COL_RECEIVER_LEDGER = "receiverLedger";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_CCY = "ccy";
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

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
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
            Object receiverIso20022 = t.getReceiver() == null ? IbanAccount.Empty : transformInstruction.getAccountOrNull(t.getReceiver());
            Object receiverLedger = t.getReceiver() == null ? "" : t.getReceiver().getPublicKey();
            list.add(new Object[]{t, true, getHighestStatus(t), receiverIso20022, receiverLedger, amt, ccy, "detail..."});
        }

        this.data = list.toArray(new Object[0][0]);
    }

    private Status getHighestStatus(Transaction t) {
        var results = new Validator().validate(t);
        var highest = Status.Ok;
        for (var r : results) {
            highest = r.getStatus().higherThan(highest) ? r.getStatus() : highest;
        }
        return highest;
    }

    public Transaction[] selectedPayments() {
        var list = new ArrayList<Transaction>();
        for (var item : this.data) {
            list.add((Transaction) item[0]);
        }
        return list.toArray(new Transaction[0]);
    }
}
