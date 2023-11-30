package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.cryptoledger.WalletCompare;
import com.radynamics.dallipay.cryptoledger.transaction.TransmissionState;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationState;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.iso20022.AccountFactory;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.PaymentEdit;
import org.apache.commons.lang3.NotImplementedException;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentTableModel extends AbstractTableModel {
    private final String[] columnNames = {COL_OBJECT, COL_VALIDATION_RESULTS, COL_SELECTOR, COL_STATUS, COL_SENDER_LEDGER, COL_RECEIVER_ACCOUNT, COL_RECEIVER_LEDGER,
            COL_BOOKED, COL_AMOUNT, COL_CCY, COL_TRX_STATUS, COL_DETAIL, COL_REMOVE};
    private final ArrayList<Record> data = new ArrayList<>();
    private Actor actor = Actor.Sender;
    private boolean editable;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public static final String COL_OBJECT = "object";
    public static final String COL_VALIDATION_RESULTS = "validationResults";
    public static final String COL_SELECTOR = "selector";
    public static final String COL_STATUS = "status";
    public static final String COL_SENDER_LEDGER = "senderLedger";
    public static final String COL_RECEIVER_ACCOUNT = "receiverAccount";
    public static final String COL_RECEIVER_LEDGER = "receiverLedger";
    public static final String COL_BOOKED = "valuta";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_CCY = "ccy";
    public static final String COL_TRX_STATUS = "transmissionStatus";
    public static final String COL_DETAIL = "detail";
    public static final String COL_REMOVE = "remove";

    public PaymentTableModel() {
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    private int getColumnIndex(String identifier) {
        return Arrays.asList(columnNames).indexOf(identifier);
    }

    public Object getValueAt(int row, int col) {
        var item = data.get(row);
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
        } else if (getColumnIndex(COL_RECEIVER_ACCOUNT) == col) {
            return item.getActorAddressOrAccount(Actor.Receiver);
        } else if (getColumnIndex(COL_RECEIVER_LEDGER) == col) {
            return item.getReceiverLedger();
        } else if (getColumnIndex(COL_BOOKED) == col) {
            return item.payment.getBooked();
        } else if (getColumnIndex(COL_AMOUNT) == col) {
            return item.getAmount(actor);
        } else if (getColumnIndex(COL_CCY) == col) {
            return item.getCcy();
        } else if (getColumnIndex(COL_TRX_STATUS) == col) {
            return item.payment.getTransmission();
        } else if (getColumnIndex(COL_DETAIL) == col) {
            return res.getString("detail");
        } else if (getColumnIndex(COL_REMOVE) == col) {
            return item.payment.getOrigin();
        }
        return null;
    }

    public void setValueAt(Object value, int row, int col) {
        var item = data.get(row);
        if (getColumnIndex(COL_VALIDATION_RESULTS) == col) {
            item.validationResults = (ValidationResult[]) value;
        } else if (getColumnIndex(COL_SELECTOR) == col) {
            item.selected = (boolean) value;
        } else if (getColumnIndex(COL_STATUS) == col) {
            item.status = (ValidationState) value;
        } else if (getColumnIndex(COL_SENDER_LEDGER) == col) {
            var cellValue = getAsValidCellValueOrNull(item.payment, value, COL_SENDER_LEDGER);
            // Invalid wallet address
            if (cellValue == null) {
                item.setSenderLedger((String) value);
            } else if (!WalletCompare.isSame(cellValue.getWallet(), item.payment.getSenderWallet())) {
                // If same keep old record with already loaded WalletInfo
                item.setSenderLedger(cellValue);
            }
        } else if (getColumnIndex(COL_RECEIVER_ACCOUNT) == col) {
            item.payment.setReceiverAccount(AccountFactory.create((String) value, item.payment.getReceiverWallet()));
        } else if (getColumnIndex(COL_RECEIVER_LEDGER) == col) {
            var cellValue = getAsValidCellValueOrNull(item.payment, value, COL_RECEIVER_LEDGER);
            // Invalid wallet address
            if (cellValue == null) {
                item.setReceiverLedger((String) value);
            } else if (!WalletCompare.isSame(cellValue.getWallet(), item.payment.getReceiverWallet())) {
                // If same keep old record with already loaded WalletInfo
                item.setReceiverLedger(cellValue);
            }
        } else if (getColumnIndex(COL_AMOUNT) == col) {
            item.setAmount((Double) value);
        } else if (getColumnIndex(COL_CCY) == col) {
            item.payment.setUserCcy(new Currency((String) value));
        } else {
            throw new NotImplementedException(String.format("Setting value for column %s is not implemented", col));
        }
        fireTableCellUpdated(row, col);
    }

    private static WalletCellValue getAsValidCellValueOrNull(Payment p, Object value, String columnName) {
        if (value instanceof WalletCellValue) {
            return (WalletCellValue) value;
        }

        // User ended cell edit
        if (value instanceof String) {
            var userInput = (String) value;
            var ledger = p.getLedger();
            var wallet = ledger.createWallet(userInput, "");
            if (ledger.createWalletValidator().isValidFormat(wallet)) {
                return new WalletCellValue(wallet, COL_RECEIVER_LEDGER.equals(columnName) ? p.getDestinationTag() : null);
            }
        }

        return null;
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        if (!editable) {
            return false;
        }
        var item = data.get(row);
        if (!PaymentEdit.create(item.payment).accountMappingEditable()) {
            return false;
        }
        if (col == getColumnIndex(COL_SELECTOR)) {
            return isSelectable(getHighestStatus(getValidationResults(row)));
        }
        if (actor == Actor.Receiver) {
            return col == getColumnIndex(COL_RECEIVER_ACCOUNT);
        }
        if (actor == Actor.Sender) {
            return col == getColumnIndex(COL_SENDER_LEDGER) || col == getColumnIndex(COL_RECEIVER_LEDGER);
        }
        return false;
    }

    public void load(Record[] data) {
        if (data == null) throw new IllegalArgumentException("Parameter 'data' cannot be null");
        this.data.clear();
        this.data.addAll(List.of(data));
        fireTableDataChanged();
    }

    public void add(Record r) {
        if (r == null) throw new IllegalArgumentException("Parameter 'r' cannot be null");
        this.data.add(r);
        fireTableDataChanged();
    }

    public void remove(Payment p) {
        if (p == null) throw new IllegalArgumentException("Parameter 'p' cannot be null");
        this.data.remove(getRowIndex(p));
        fireTableDataChanged();
    }

    public void setValidationResults(Payment p, ValidationResult[] validationResults) {
        var rowIndex = getRowIndex(p);

        setValueAt(validationResults, rowIndex, getColumnIndex(COL_VALIDATION_RESULTS));
        var highestStatus = getHighestStatus(validationResults);
        setValueAt(isSelected(p, highestStatus), rowIndex, getColumnIndex(COL_SELECTOR));
        setValueAt(highestStatus, rowIndex, getColumnIndex(COL_STATUS));
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

    public Payment[] payments() {
        var list = new ArrayList<Payment>();
        for (var item : this.data) {
            list.add(item.payment);
        }
        return list.toArray(new Payment[0]);
    }

    public Payment[] checkedPayments() {
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

    public void onTransactionChanged(Payment t) {
        int row = getRowIndex(t);
        fireTableRowsUpdated(row, row);
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

    public boolean getEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
