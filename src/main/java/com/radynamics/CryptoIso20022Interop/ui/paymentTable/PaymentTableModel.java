package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.TransactionValidator;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class PaymentTableModel extends AbstractTableModel {
    private final String[] columnNames = {COL_OBJECT, COL_VALIDATION_RESULTS, COL_SELECTOR, COL_STATUS, COL_RECEIVER_ISO20022, COL_RECEIVER_LEDGER,
            COL_BOOKED, COL_AMOUNT, COL_CCY, COL_TRX_STATUS, COL_DETAIL};
    private Object[][] data;
    private final TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;
    private TransactionValidator validator;
    private Actor actor = Actor.Sender;

    public static final String COL_OBJECT = "object";
    public static final String COL_VALIDATION_RESULTS = "validationResults";
    public static final String COL_SELECTOR = "selector";
    public static final String COL_STATUS = "status";
    public static final String COL_RECEIVER_ISO20022 = "receiverIso20022";
    public static final String COL_RECEIVER_LEDGER = "receiverLedger";
    public static final String COL_BOOKED = "valuta";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_CCY = "ccy";
    public static final String COL_TRX_STATUS = "transmissionStatus";
    public static final String COL_DETAIL = "detail";

    public PaymentTableModel(TransformInstruction transformInstruction, CurrencyConverter currencyConverter, TransactionValidator validator) {
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;
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

    public void load(Transaction[] data) {
        ArrayList<Object[]> list = new ArrayList<>();
        for (var t : data) {
            var ccy = transformInstruction.getTargetCcy();
            Object actorAddressOrAccount = getActorAddressOrAccount(t);
            Object actorLedger = getActorWalletText(t);
            list.add(new Object[]{t, new ValidationResult[0], true, null, actorAddressOrAccount, actorLedger, t.getBooked(), null, ccy, t.getTransmission(), "detail..."});
        }

        this.data = list.toArray(new Object[0][0]);
        fireTableDataChanged();

        int row = 0;
        for (var t : data) {
            validateAsync(t, row);
            loadTargetCcyAmountAsync(t, row);
            row++;
        }
    }

    private Object getActorAddressOrAccount(Transaction t) {
        Object actorAddressOrAccount = actor.get(t.getReceiverAddress(), t.getSenderAddress());
        if (actorAddressOrAccount == null) {
            var actorAccount = actor.get(t.getReceiverAccount(), t.getSenderAccount());
            actorAddressOrAccount = actorAccount == null ? IbanAccount.Empty : actorAccount;
        }
        return actorAddressOrAccount;
    }

    private String getActorWalletText(Transaction t) {
        var wallet = actor.get(t.getReceiverWallet(), t.getSenderWallet());
        return wallet == null ? "" : wallet.getPublicKey();
    }

    private void validateAsync(Transaction t, int row) {
        var completableFuture = new CompletableFuture<ImmutablePair<Integer, ValidationResult[]>>();
        completableFuture.thenAccept(result -> {
            var rowIndex = result.left;
            var validationResults = result.right;

            setValueAt(validationResults, rowIndex, getColumnIndex(COL_VALIDATION_RESULTS));
            var highestStatus = getHighestStatus(validationResults);
            setValueAt(isSelected(highestStatus), rowIndex, getColumnIndex(COL_SELECTOR));
            setValueAt(highestStatus, rowIndex, getColumnIndex(COL_STATUS));
        });

        Executors.newCachedThreadPool().submit(() -> {
            completableFuture.complete(new ImmutablePair<>(row, validator.validate(t)));
        });
    }

    private void loadTargetCcyAmountAsync(Transaction t, int row) {
        var completableFuture = new CompletableFuture<ImmutablePair<Integer, Double>>();
        completableFuture.thenAccept(result -> {
            var rowIndex = result.left;
            var amt = result.right;

            setValueAt(amt, rowIndex, getColumnIndex(COL_AMOUNT));
        });

        Executors.newCachedThreadPool().submit(() -> {
            var ccy = transformInstruction.getTargetCcy();
            var ccyPair = new CurrencyPair(t.getCcy(), ccy);
            CurrencyConverter cc;
            if (actor == Actor.Sender) {
                currencyConverter.convert(t.getLedger().convertToNativeCcyAmount(t.getAmountSmallestUnit()), t.getCcy(), ccy);
                cc = currencyConverter;
            } else {
                var source = transformInstruction.getHistoricExchangeRateSource();
                var rate = source.rateAt(ccyPair, t.getBooked());
                cc = new CurrencyConverter(new ExchangeRate[]{rate});
            }
            var amt = cc.convert(t.getLedger().convertToNativeCcyAmount(t.getAmountSmallestUnit()), t.getCcy(), ccy);
            completableFuture.complete(new ImmutablePair<>(row, amt));
        });
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
        validateAsync(t, row);
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }
}
