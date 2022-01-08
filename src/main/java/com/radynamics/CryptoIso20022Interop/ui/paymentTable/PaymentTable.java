package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Status;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.ui.TableColumnBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PaymentTable extends JPanel {
    private final JTable table;
    private final PaymentTableModel model;
    private TransformInstruction transformInstruction;

    public PaymentTable(TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        super(new GridLayout(1, 0));
        this.transformInstruction = transformInstruction;

        model = new PaymentTableModel(transformInstruction, currencyConverter);

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Status.class, new PaymentStatusCellRenderer(table.getColumn(PaymentTableModel.COL_VALIDATION_RESULTS)));
        table.setDefaultRenderer(IbanAccount.class, new AccountCellRenderer());

        table.setRowHeight(30);
        initColumns();

        add(new JScrollPane(table));
    }

    private void initColumns() {
        var cb = new TableColumnBuilder(table);
        cb.forColumn(PaymentTableModel.COL_OBJECT).headerCenter().hide();
        cb.forColumn(PaymentTableModel.COL_VALIDATION_RESULTS).headerCenter().hide();
        cb.forColumn(PaymentTableModel.COL_SELECTOR).headerValue("").fixedWidth(40);
        cb.forColumn(PaymentTableModel.COL_STATUS).headerValue("").fixedWidth(40).headerCenter();
        cb.forColumn(PaymentTableModel.COL_RECEIVER_ISO20022).headerValue("Receiver from Input").width(200);
        cb.forColumn(PaymentTableModel.COL_RECEIVER_LEDGER).headerValue("Receiver CryptoCurrency Wallet").width(200);
        {
            var c = cb.forColumn(PaymentTableModel.COL_AMOUNT).headerValue("Amount").width(100).headerRigth().getColumn();
            c.setCellRenderer(new AmountCellRenderer(transformInstruction, table.getColumn(PaymentTableModel.COL_OBJECT)));
        }
        cb.forColumn(PaymentTableModel.COL_CCY).headerValue("").maxWidth(50);
        {
            var c = cb.forColumn(PaymentTableModel.COL_DETAIL).headerValue("").maxWidth(50).headerCenter().getColumn();
            c.setCellRenderer(new ShowDetailCellRenderer());
        }

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showMore(table, getSelectedRow(table));
                    return;
                }

                var clickedColumn = table.getColumnModel().getColumn(table.columnAtPoint(e.getPoint()));
                if (!StringUtils.equals((String) clickedColumn.getIdentifier(), PaymentTableModel.COL_DETAIL)) {
                    return;
                }

                if (e.getClickCount() == 1) {
                    showMore(table, getSelectedRow(table));
                }
            }
        });
    }

    public void load(Transaction[] data) {
        model.load(data);
    }

    private Transaction getSelectedRow(JTable table) {
        var row = table.getSelectedRow();
        var col = table.getColumn(PaymentTableModel.COL_OBJECT).getModelIndex();
        return (Transaction) table.getModel().getValueAt(row, col);
    }

    private void showMore(JTable table, Transaction obj) {
        JOptionPane.showMessageDialog(table, String.format("TODO: RST 2022-01-06 show more details for %s", obj.getReceiverAccount().getUnformatted()));
    }

    public Transaction[] selectedPayments() {
        return model.selectedPayments();
    }
}
