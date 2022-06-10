package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.db.AccountMapping;
import com.radynamics.CryptoIso20022Interop.db.AccountMappingRepo;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import com.radynamics.CryptoIso20022Interop.exchange.HistoricExchangeRateLoader;
import com.radynamics.CryptoIso20022Interop.iso20022.*;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class PaymentTable extends JPanel {
    final static Logger log = LogManager.getLogger(PaymentTable.class);
    private final JTable table;
    private final PaymentTableModel model;
    private TransformInstruction transformInstruction;
    private final Actor actor;
    private Payment[] data = new Payment[0];
    private PaymentValidator validator;
    private ArrayList<ProgressListener> progressListener = new ArrayList<>();
    private ArrayList<ChangedListener> selectorChangedListener = new ArrayList<>();
    private ArrayList<RefreshListener> refreshListener = new ArrayList<>();

    public PaymentTable(TransformInstruction transformInstruction, CurrencyConverter currencyConverter, Actor actor, PaymentValidator validator) {
        super(new GridLayout(1, 0));
        this.transformInstruction = transformInstruction;
        this.actor = actor;
        this.validator = validator;

        var exchangeRateLoader = new HistoricExchangeRateLoader(transformInstruction, currencyConverter);
        model = new PaymentTableModel(exchangeRateLoader, validator);
        model.setActor(actor);
        model.addProgressListener(progress -> raiseProgress(progress));
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_SELECTOR)) {
                    raiseSelectorChanged();
                }
            }
        });

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(ValidationState.class, new ValidationStateCellRenderer(table.getColumn(PaymentTableModel.COL_VALIDATION_RESULTS)));
        table.setDefaultRenderer(TransmissionState.class, new TransmissionCellRenderer());
        table.setDefaultRenderer(IbanAccount.class, new AccountCellRenderer());
        table.setDefaultRenderer(OtherAccount.class, new AccountCellRenderer());
        table.setDefaultRenderer(Address.class, new AddressCellRenderer());
        table.setDefaultRenderer(ZonedDateTime.class, new DateTimeCellRenderer());
        var objectColumn = table.getColumn(PaymentTableModel.COL_OBJECT);
        var cellEditor = new WalletCellEditor(objectColumn, actor == Actor.Sender);
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
            c.setCellRenderer(new AmountCellRenderer(transformInstruction, table.getColumn(PaymentTableModel.COL_OBJECT)));
        }
        {
            var c = cb.forColumn(PaymentTableModel.COL_CCY).headerValue("").maxWidth(50).getColumn();
            c.setCellRenderer(new DefaultTableCellRenderer());
        }
        cb.forColumn(PaymentTableModel.COL_TRX_STATUS).headerValue("").maxWidth(50);
        {
            var c = cb.forColumn(PaymentTableModel.COL_DETAIL).headerValue("").maxWidth(50).headerCenter().getColumn();
            c.setCellRenderer(new ShowDetailCellRenderer());
        }

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showMore(getSelectedRow(table));
                    return;
                }

                var clickedColumn = table.getColumnModel().getColumn(table.columnAtPoint(e.getPoint()));
                if (!StringUtils.equals((String) clickedColumn.getIdentifier(), PaymentTableModel.COL_DETAIL)) {
                    return;
                }

                if (e.getClickCount() == 1) {
                    showMore(getSelectedRow(table));
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
        var cleanedInput = tcl.getNewValue().toString().trim();
        var row = tcl.getRow();
        var t = (Payment) model.getValueAt(row, table.getColumnModel().getColumnIndex(PaymentTableModel.COL_OBJECT));

        var editedActor = getEditedActor(tcl.getColumn());
        AccountMapping mapping = new AccountMapping(t.getLedger().getId());
        Account a = null;
        if (tcl.getOldValue() instanceof Account) {
            a = (Account) tcl.getOldValue();
        } else if (tcl.getOldValue() instanceof WalletCellValue) {
            // While sending payments user is able to change "Receiver from Input". Account is not changeable.
            a = editedActor == Actor.Sender ? t.getSenderAccount() : t.getReceiverAccount();
        }
        var w = editedActor == Actor.Sender ? t.getSenderWallet() : t.getReceiverWallet();
        mapping.setAccount(a);
        mapping.setWallet(w);
        try (var repo = new AccountMappingRepo()) {
            mapping = repo.single(t.getLedger().getId(), a, w).orElse(mapping);
        } catch (Exception ex) {
            ExceptionDialog.show(table, ex);
        }

        ChangedValue changedValue = null;
        if (tcl.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_SENDER_LEDGER)) {
            changedValue = ChangedValue.SenderWallet;
            mapping.setWallet(createWalletOrNull(cleanedInput));
        }
        if (tcl.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_SENDER_ACCOUNT)) {
            changedValue = ChangedValue.SenderAccount;
            mapping.setAccount((Account) tcl.getNewValue());
        }
        if (tcl.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_RECEIVER_ACCOUNT)) {
            changedValue = ChangedValue.ReceiverAccount;
            mapping.setAccount((Account) tcl.getNewValue());
        }
        if (tcl.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_RECEIVER_LEDGER)) {
            changedValue = ChangedValue.ReceiverWallet;
            mapping.setWallet(createWalletOrNull(cleanedInput));
        }

        if (changedValue == null) {
            return;
        }

        try (var repo = new AccountMappingRepo()) {
            if (mapping.allPresent()) {
                // When user clicks into cell and predefined value (ex senderWallet) matches other one (ex senderAccount).
                if (mapping.bothSame()) {
                    if (mapping.isPersisted()) {
                        repo.delete(mapping);
                    }
                } else {
                    repo.saveOrUpdate(mapping);
                }
            } else if (mapping.isPersisted() && mapping.accountOrWalletMissing()) {
                // Interpret "" as removal. During creation values are maybe not yet defined.
                repo.delete(mapping);
            }
            repo.commit();
        } catch (Exception ex) {
            ExceptionDialog.show(table, ex);
        }

        // Update all affected payments
        var mi = new MappingInfo(mapping, changedValue);
        for (var p : data) {
            if (mi.apply(p)) {
                // Ensure a newly entered senderWallet's history is loaded for following validation calls.
                validator.getHistoryValidator().loadHistory(new Payment[]{p});
                model.onAccountOrWalletsChanged(p);
            }
        }
    }

    private Actor getEditedActor(int col) {
        if (col == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_SENDER_LEDGER)
                || col == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_SENDER_ACCOUNT)) {
            return Actor.Sender;
        }

        return Actor.Receiver;
    }

    private Wallet createWalletOrNull(String text) {
        return StringUtils.isEmpty(text) ? null : transformInstruction.getLedger().createWallet(text, null);
    }

    public void load(Payment[] data) {
        this.data = data;
        model.load(toRecords(data));
        table.revalidate();
        table.repaint();
    }

    private Record[] toRecords(Payment[] data) {
        var list = new ArrayList<Record>();
        for (var o : data) {
            list.add(new Record(o));
        }
        return list.toArray(new Record[0]);
    }

    private Payment getSelectedRow(JTable table) {
        var row = table.getSelectedRow();
        var col = table.getColumn(PaymentTableModel.COL_OBJECT).getModelIndex();
        return (Payment) table.getModel().getValueAt(row, col);
    }

    private void showMore(Payment obj) {
        var frm = new PaymentDetailForm(obj, validator, getExchangeRateProvider());
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(650, 430);
        frm.setModal(true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);

        if (frm.getPaymentChanged()) {
            refresh(obj);
        }
    }

    private ExchangeRateProvider getExchangeRateProvider() {
        return actor == Actor.Sender
                ? transformInstruction.getExchangeRateProvider()
                : transformInstruction.getHistoricExchangeRateSource();
    }

    public Payment[] selectedPayments() {
        return model.selectedPayments();
    }

    public ValidationResult[] getValidationResults(Payment[] payments) {
        return model.getValidationResults(payments);
    }

    public void refresh(Payment t) {
        var row = getRow(t);
        if (row == -1) {
            log.warn(String.format("Could not find %s in table.", t.getReceiverAccount().getUnformatted()));
            return;
        }
        raiseRefresh(t);
        model.onTransactionChanged(t);
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
}
