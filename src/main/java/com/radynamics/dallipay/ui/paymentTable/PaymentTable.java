package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.cryptoledger.BalanceRefresher;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletCompare;
import com.radynamics.dallipay.cryptoledger.transaction.TransmissionState;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationState;
import com.radynamics.dallipay.db.AccountMapping;
import com.radynamics.dallipay.db.AccountMappingRepo;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.HistoricExchangeRateLoader;
import com.radynamics.dallipay.iso20022.*;
import com.radynamics.dallipay.transformation.TransactionTranslator;
import com.radynamics.dallipay.transformation.TransformInstruction;
import com.radynamics.dallipay.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class PaymentTable extends JPanel {
    final static Logger log = LogManager.getLogger(PaymentTable.class);
    private final JTable table;
    private final PaymentTableModel model;
    private TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;
    private final Actor actor;
    private PaymentValidator validator;
    private ArrayList<ProgressListener> progressListener = new ArrayList<>();
    private final ArrayList<MappingChangedListener> mappingChangedListener = new ArrayList<>();
    private ArrayList<ChangedListener> selectorChangedListener = new ArrayList<>();
    private ArrayList<RefreshListener> refreshListener = new ArrayList<>();
    private final ArrayList<PaymentListener> paymentListener = new ArrayList<>();
    private final DataLoader dataLoader;

    public PaymentTable(TransformInstruction transformInstruction, CurrencyConverter currencyConverter, Actor actor, PaymentValidator validator, TransactionTranslator transactionTranslator) {
        super(new GridLayout(1, 0));
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;
        this.actor = actor;
        this.validator = validator;

        var exchangeRateLoader = new HistoricExchangeRateLoader(transformInstruction, currencyConverter);
        model = new PaymentTableModel();
        table = new JTable(model);
        model.setActor(actor);
        dataLoader = new DataLoader(model, exchangeRateLoader, validator, transactionTranslator);
        dataLoader.addProgressListener(progress -> {
            table.revalidate();
            table.repaint();
            raiseProgress(progress);
        });
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_SELECTOR)) {
                    raiseSelectorChanged();
                }
            }
        });

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(ValidationState.class, new ValidationStateCellRenderer(table.getColumn(PaymentTableModel.COL_VALIDATION_RESULTS)));
        table.setDefaultRenderer(TransmissionState.class, new TransmissionCellRenderer());
        table.setDefaultRenderer(IbanAccount.class, new AccountCellRenderer());
        table.setDefaultRenderer(OtherAccount.class, new AccountCellRenderer());
        table.setDefaultRenderer(Address.class, new AddressCellRenderer());
        table.setDefaultRenderer(ZonedDateTime.class, new DateTimeCellRenderer());
        var objectColumn = table.getColumn(PaymentTableModel.COL_OBJECT);
        var cellEditor = new WalletCellEditor(this, objectColumn, actor == Actor.Sender);
        table.getColumnModel().getColumn(model.findColumn(PaymentTableModel.COL_SENDER_LEDGER)).setCellEditor(cellEditor);
        table.getColumnModel().getColumn(model.findColumn(PaymentTableModel.COL_RECEIVER_LEDGER)).setCellEditor(cellEditor);

        table.setRowHeight(30);
        initColumns();

        var selectorColumn = table.getColumn(PaymentTableModel.COL_SELECTOR);
        new MultiRowChecker(table, selectorColumn, row -> model.isCellEditable(row, selectorColumn.getModelIndex()));

        add(new JScrollPane(table));
    }

    private void initColumns() {
        var cb = new TableColumnBuilder(table);
        cb.forColumn(PaymentTableModel.COL_OBJECT).headerCenter().hide();
        cb.forColumn(PaymentTableModel.COL_VALIDATION_RESULTS).headerCenter().hide();
        cb.forColumn(PaymentTableModel.COL_SELECTOR).headerValue("").fixedWidth(40);
        {
            var c = cb.forColumn(PaymentTableModel.COL_STATUS).headerValue("").fixedWidth(40).headerCenter().getColumn();
            c.setCellRenderer(new ValidationStateCellRenderer(table.getColumn(PaymentTableModel.COL_VALIDATION_RESULTS)));
        }
        {
            var c = cb.forColumn(PaymentTableModel.COL_SENDER_LEDGER).headerValue("Sender Wallet").width(200).getColumn();
            c.setCellRenderer(new WalletCellRenderer());
        }
        {
            var c = cb.forColumn(PaymentTableModel.COL_SENDER_ACCOUNT).headerValue("Sender Account").width(200).getColumn();
            c.setCellEditor(new AccountCellEditor(true));
            c.setCellRenderer(new AccountCellRenderer());
            if (actor == Actor.Sender) {
                cb.hide();
            }
        }
        {
            var c = cb.forColumn(PaymentTableModel.COL_RECEIVER_ACCOUNT).headerValue("Receiver Account").width(200).getColumn();
            c.setCellEditor(new AccountCellEditor(true));
            c.setCellRenderer(new AccountCellRenderer());
        }
        {
            var c = cb.forColumn(PaymentTableModel.COL_RECEIVER_LEDGER).headerValue("Receiver Wallet").width(200).getColumn();
            c.setCellRenderer(new WalletCellRenderer());
        }
        {
            var c = cb.forColumn(PaymentTableModel.COL_BOOKED).headerValue("Booked").width(90).getColumn();
            c.setCellRenderer(new DateTimeCellRenderer());
            if (model.getActor() == Actor.Sender) {
                cb.hide();
            }
        }
        {
            var c = cb.forColumn(PaymentTableModel.COL_AMOUNT).headerValue("Amount").width(100).headerRigth().getColumn();
            c.setCellRenderer(new AmountCellRenderer(table.getColumn(PaymentTableModel.COL_OBJECT)));
        }
        {
            var c = cb.forColumn(PaymentTableModel.COL_CCY).headerValue("").maxWidth(50).getColumn();
            c.setCellRenderer(new CurrencyCellRenderer(table.getColumn(PaymentTableModel.COL_OBJECT)));
        }
        cb.forColumn(PaymentTableModel.COL_TRX_STATUS).headerValue("").maxWidth(50);
        {
            var c = cb.forColumn(PaymentTableModel.COL_DETAIL).headerValue("").maxWidth(50).headerCenter().getColumn();
            c.setCellRenderer(new ShowDetailCellRenderer());
        }
        {
            var c = cb.forColumn(PaymentTableModel.COL_REMOVE).headerValue("").maxWidth(25).headerCenter().getColumn();
            c.setCellRenderer(new RemoveCellRenderer());
        }

        var owner = this;
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showMore(getSelectedRow(table));
                    return;
                }

                if (e.getClickCount() != 1) {
                    return;
                }

                var clickedColumn = table.getColumnModel().getColumn(table.columnAtPoint(e.getPoint()));
                if (StringUtils.equals((String) clickedColumn.getIdentifier(), PaymentTableModel.COL_DETAIL)) {
                    showMore(getSelectedRow(table));
                    return;

                }
                if (StringUtils.equals((String) clickedColumn.getIdentifier(), PaymentTableModel.COL_REMOVE)) {
                    var p = getSelectedRow(table);
                    if (p.getOrigin().isDeletable()) {
                        raiseRemove(p);
                    }
                    return;
                }
            }
        });
        new TableCellListener(table, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                onCellEdited((TableCellListener) e.getSource());
            }
        });
    }

    private void onCellEdited(TableCellListener tcl) {
        var row = tcl.getRow();
        var t = (Payment) model.getValueAt(row, table.getColumnModel().getColumnIndex(PaymentTableModel.COL_OBJECT));

        if (tcl.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_SENDER_LEDGER)) {
            onWalletEdited(t, tcl, ChangedValue.SenderWallet);
        }
        if (tcl.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_SENDER_ACCOUNT)) {
            onAccountEdited(t, tcl, ChangedValue.SenderAccount);
        }
        if (tcl.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_RECEIVER_ACCOUNT)) {
            onAccountEdited(t, tcl, ChangedValue.ReceiverAccount);
        }
        if (tcl.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_RECEIVER_LEDGER)) {
            onWalletEdited(t, tcl, ChangedValue.ReceiverWallet);
        }
    }

    private void onWalletEdited(Payment payment, TableCellListener tcl, ChangedValue changedValue) {
        onWalletEdited(payment, createWalletOrNull(tcl.getNewValue()), changedValue);
    }

    private void onWalletEdited(Payment payment, Wallet newWallet, ChangedValue changedValue) {
        // While sending payments user is able to change "Receiver from Input". Account is not changeable.
        var account = changedValue == ChangedValue.SenderWallet ? payment.getSenderAccount() : payment.getReceiverAccount();

        var mapping = new AccountMapping(payment.getLedger().getId());
        mapping.setAccount(account);
        try (var repo = new AccountMappingRepo()) {
            // User maps a wallet to an account number
            mapping = repo.single(payment.getLedger().getId(), account).orElse(mapping);
        } catch (Exception ex) {
            ExceptionDialog.show(table, ex);
        }
        mapping.setWallet(newWallet);
        persistOrDelete(mapping);

        var br = new BalanceRefresher(transformInstruction.getNetwork());
        br.refresh(payment.getLedger(), newWallet);

        raiseWalletMappingChanged(new MappingInfo(mapping, changedValue));
    }

    private void onAccountEdited(Payment t, TableCellListener tcl, ChangedValue changedValue) {
        var newAccount = (Account) tcl.getNewValue();
        var wallet = changedValue == ChangedValue.SenderAccount ? t.getSenderWallet() : t.getReceiverWallet();

        var mapping = new AccountMapping(t.getLedger().getId());
        mapping.setWallet(wallet);
        try (var repo = new AccountMappingRepo()) {
            // User maps an account number to a wallet
            mapping = repo.single(t.getLedger().getId(), wallet).orElse(mapping);
        } catch (Exception ex) {
            ExceptionDialog.show(table, ex);
        }
        mapping.setAccount(newAccount);
        persistOrDelete(mapping);

        accountMappingChanged(new MappingInfo(mapping, changedValue));
    }

    private void accountMappingChanged(MappingInfo mi) {
        // Update all affected payments
        for (var p : model.payments()) {
            if (mi.apply(p)) {
                getDataLoader().onAccountOrWalletsChanged(p);
            }
        }
    }

    private void persistOrDelete(AccountMapping mapping) {
        try (var repo = new AccountMappingRepo()) {
            repo.persistOrDelete(mapping);
            repo.commit();
        } catch (Exception ex) {
            ExceptionDialog.show(table, ex);
        }
    }

    private Wallet createWalletOrNull(Object newValue) {
        if (newValue instanceof WalletCellValue) {
            return ((WalletCellValue) newValue).getWallet();
        }

        var text = newValue.toString().trim();
        return StringUtils.isEmpty(text) ? null : transformInstruction.getLedger().createWallet(text, null);
    }

    public void load(Payment[] data) {
        var records = toRecords(data);
        model.load(records);
        dataLoader.loadAsync(records);
    }

    public void add(Payment p) {
        var record = toRecord(p);
        model.add(record);
        dataLoader.loadAsync(record);
    }

    public void remove(Payment p) {
        model.remove(p);
    }

    private Record[] toRecords(Payment[] data) {
        var list = new ArrayList<Record>();
        for (var o : data) {
            list.add(toRecord(o));
        }
        return list.toArray(new Record[0]);
    }

    private Record toRecord(Payment p) {
        return new Record(p);
    }

    private Payment getSelectedRow(JTable table) {
        var row = table.getSelectedRow();
        var col = table.getColumn(PaymentTableModel.COL_OBJECT).getModelIndex();
        return (Payment) table.getModel().getValueAt(row, col);
    }

    private void showMore(Payment obj) {
        var senderWallet = obj.getSenderWallet();
        var receiverWallet = obj.getReceiverWallet();
        var frm = PaymentDetailForm.showModal(this, obj, validator, getExchangeRateProvider(), currencyConverter, actor, model.getEditable());
        if (frm.getPaymentChanged()) {
            if (!WalletCompare.isSame(senderWallet, obj.getSenderWallet())) {
                onWalletEdited(obj, obj.getSenderWallet(), ChangedValue.SenderWallet);
            }
            if (!WalletCompare.isSame(receiverWallet, obj.getReceiverWallet())) {
                onWalletEdited(obj, obj.getReceiverWallet(), ChangedValue.ReceiverWallet);
            }
            refresh(obj);
        }
    }

    private ExchangeRateProvider getExchangeRateProvider() {
        return actor == Actor.Sender
                ? transformInstruction.getExchangeRateProvider()
                : transformInstruction.getHistoricExchangeRateSource();
    }

    public Payment[] checkedPayments() {
        return model.checkedPayments();
    }

    public Payment[] selectedPayments() {
        return new Payment[]{getSelectedRow(table)};
    }

    public ValidationResult[] getValidationResults(Payment[] payments) {
        return model.getValidationResults(payments);
    }

    public CompletableFuture<Void> refresh(Payment[] payments) {
        var list = new ArrayList<CompletableFuture<Void>>();
        for (var p : payments) {
            list.add(refresh(p));
        }

        var f = new CompletableFuture<Void>();
        Executors.newCachedThreadPool().submit(() -> {
            CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
            f.complete(null);
        });

        return f;
    }

    public CompletableFuture<Void> refresh(Payment t) {
        var row = getRow(t);
        if (row == -1) {
            log.warn(String.format("Could not find %s in table.", t.getReceiverAccount().getUnformatted()));
            return CompletableFuture.completedFuture(null);
        }
        raiseRefresh(t);
        // Ensure changed wallets are refreshed in dataLoader.loadWalletInfoAsync
        dataLoader.onAccountOrWalletsChangedAsync(t)
                .thenRun(() -> model.onTransactionChanged(t));
        return dataLoader.onTransactionChanged(t);
    }

    private int getRow(Payment t) {
        var col = table.getColumn(PaymentTableModel.COL_OBJECT).getModelIndex();
        for (var row = 0; row < table.getModel().getRowCount(); row++) {
            if (table.getModel().getValueAt(row, col) == t) {
                return row;
            }
        }
        return -1;
    }

    public void addProgressListener(ProgressListener l) {
        progressListener.add(l);
    }

    private void raiseProgress(Progress progress) {
        for (var l : progressListener) {
            l.onProgress(progress);
        }
    }

    public void addSelectorChangedListener(ChangedListener l) {
        selectorChangedListener.add(l);
    }

    private void raiseSelectorChanged() {
        for (var l : selectorChangedListener) {
            l.onChanged();
        }
    }

    public void addRefreshListener(RefreshListener l) {
        refreshListener.add(l);
    }

    private void raiseRefresh(Payment p) {
        for (var l : refreshListener) {
            l.onRefresh(p);
        }
    }

    public void addPaymentListener(PaymentListener l) {
        paymentListener.add(l);
    }

    private void raiseRemove(Payment p) {
        for (var l : paymentListener) {
            l.onRemove(p);
        }
    }

    public void addMappingChangedListener(MappingChangedListener l) {
        mappingChangedListener.add(l);
    }

    private void raiseWalletMappingChanged(MappingInfo mi) {
        for (var l : mappingChangedListener) {
            l.onWalletChanged(mi);
        }
    }

    public void setEditable(boolean editable) {
        model.setEditable(editable);
    }

    public DataLoader getDataLoader() {
        return dataLoader;
    }
}
