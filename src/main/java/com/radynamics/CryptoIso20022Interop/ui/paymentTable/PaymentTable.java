package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.exchange.HistoricExchangeRateLoader;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.*;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.ui.PaymentDetailForm;
import com.radynamics.CryptoIso20022Interop.ui.TableColumnBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;

public class PaymentTable extends JPanel {
    private final JTable table;
    private final PaymentTableModel model;
    private TransformInstruction transformInstruction;
    private PaymentValidator validator;

    public PaymentTable(TransformInstruction transformInstruction, CurrencyConverter currencyConverter, Actor actor, PaymentValidator validator) {
        super(new GridLayout(1, 0));
        this.transformInstruction = transformInstruction;
        this.validator = validator;

        var exchangeRateLoader = new HistoricExchangeRateLoader(transformInstruction, currencyConverter);
        model = new PaymentTableModel(exchangeRateLoader, validator);
        model.setActor(actor);

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(ValidationState.class, new ValidationStateCellRenderer(table.getColumn(PaymentTableModel.COL_VALIDATION_RESULTS)));
        table.setDefaultRenderer(TransmissionState.class, new TransmissionCellRenderer());
        table.setDefaultRenderer(IbanAccount.class, new AccountCellRenderer());
        table.setDefaultRenderer(OtherAccount.class, new AccountCellRenderer());
        table.setDefaultRenderer(Address.class, new AddressCellRenderer());
        table.setDefaultRenderer(LocalDateTime.class, new LocalDateTimeCellRenderer());
        {
            var column = table.getColumnModel().getColumn(model.findColumn(PaymentTableModel.COL_RECEIVER_ISO20022));
            if (actor == Actor.Receiver) {
                column.setCellEditor(new AccountCellEditor(true));
                column.setCellRenderer(new AccountCellRenderer());
            }
        }
        var lookupProvider = transformInstruction.getLedger().getLookupProvider();
        var objectColumn = table.getColumn(PaymentTableModel.COL_OBJECT);
        var cellEditor = new ReceiverLedgerCellEditor(objectColumn, lookupProvider, actor == Actor.Sender);
        table.getColumnModel().getColumn(model.findColumn(PaymentTableModel.COL_RECEIVER_LEDGER)).setCellEditor(cellEditor);

        table.setRowHeight(30);
        initColumns();

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
            var headerValue = model.getActor().get("Receiver from Input", "Sender for Export");
            cb.forColumn(PaymentTableModel.COL_RECEIVER_ISO20022).headerValue(headerValue).width(200);
        }
        {
            var headerValue = String.format("%s CryptoCurrency Wallet", model.getActor().get("Receiver", "Sender"));
            cb.forColumn(PaymentTableModel.COL_RECEIVER_LEDGER).headerValue(headerValue).width(200);
        }
        {
            var c = cb.forColumn(PaymentTableModel.COL_BOOKED).headerValue("Booked").width(90).getColumn();
            c.setCellRenderer(new LocalDateTimeCellRenderer());
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
                var tcl = (TableCellListener) e.getSource();
                var cleanedInput = tcl.getNewValue().toString().trim();
                var row = tcl.getRow();
                var t = (Payment) model.getValueAt(row, table.getColumnModel().getColumnIndex(PaymentTableModel.COL_OBJECT));

                if (tcl.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_RECEIVER_ISO20022)) {
                    t.setSenderAccount(AccountFactory.create(cleanedInput));
                    model.onTransactionChanged(row, t);
                }
                if (tcl.getColumn() == table.getColumnModel().getColumnIndex(PaymentTableModel.COL_RECEIVER_LEDGER)) {
                    t.setReceiverWallet(transformInstruction.getLedger().createWallet(cleanedInput, null));
                    model.onTransactionChanged(row, t);
                }
            }
        });
    }

    public void load(Payment[] data) {
        model.load(data);
        table.revalidate();
        table.repaint();
    }

    private Payment getSelectedRow(JTable table) {
        var row = table.getSelectedRow();
        var col = table.getColumn(PaymentTableModel.COL_OBJECT).getModelIndex();
        return (Payment) table.getModel().getValueAt(row, col);
    }

    private void showMore(Payment obj) {
        var frm = new PaymentDetailForm(obj, validator);
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(650, 430);
        frm.setModal(true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }

    public Payment[] selectedPayments() {
        return model.selectedPayments();
    }

    public void refresh(Payment t) {
        var row = getRow(t);
        if (row == -1) {
            LogManager.getLogger().warn(String.format("Could not find %s in table.", t.getReceiverAccount().getUnformatted()));
            return;
        }
        model.onTransactionChanged(row, t);
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
}
