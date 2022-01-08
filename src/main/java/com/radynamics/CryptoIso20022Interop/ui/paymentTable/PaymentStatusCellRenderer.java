package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Status;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class PaymentStatusCellRenderer extends JLabel implements TableCellRenderer {
    private TableColumn validationResultsColumn;

    private final static int WIDTH = 16;
    private final static int HEIGHT = 16;

    private final static FlatSVGIcon ok = null;
    private final static FlatSVGIcon info = new FlatSVGIcon("svg/informationDialog.svg", WIDTH, HEIGHT);
    private final static FlatSVGIcon warning = new FlatSVGIcon("svg/warningDialog.svg", WIDTH, HEIGHT);
    private final static FlatSVGIcon error = new FlatSVGIcon("svg/errorDialog.svg", WIDTH, HEIGHT);

    public PaymentStatusCellRenderer(TableColumn validationResultsColumn) {
        this.validationResultsColumn = validationResultsColumn;
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        var status = (Status) value;
        var obj = (ValidationResult[]) table.getModel().getValueAt(row, validationResultsColumn.getModelIndex());

        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setHorizontalAlignment(JLabel.CENTER);
        setIcon(getIconOrNull(status));
        setToolTipText(createToolTipText(obj));
        return this;
    }

    private String createToolTipText(ValidationResult[] results) {
        var sb = new StringBuilder();
        for (var i = 0; i < results.length; i++) {
            sb.append(String.format("- %s", results[i].getMessage()));
            if (i < results.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static Icon getIconOrNull(Status status) {
        switch (status) {
            case Ok -> {
                return ok;
            }
            case Info -> {
                return info;
            }
            case Warning -> {
                return warning;
            }
            case Error -> {
                return error;
            }

            default -> throw new IllegalStateException("Unexpected value: " + status);
        }
    }
}
