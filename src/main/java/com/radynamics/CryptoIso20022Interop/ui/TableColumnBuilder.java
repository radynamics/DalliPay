package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.ui.paymentTable.HorizontalAlignmentHeaderRenderer;

import javax.swing.*;
import javax.swing.table.TableColumn;

public class TableColumnBuilder {
    private JTable table;
    private TableColumn column;

    public TableColumnBuilder(JTable table) {
        this.table = table;
    }

    public TableColumnBuilder forColumn(String identifier) {
        column = table.getColumn(identifier);
        return setIdentifier(identifier).headerAlignment(SwingConstants.LEFT);
    }

    private TableColumnBuilder setIdentifier(String identifier) {
        column.setIdentifier(identifier);
        return this;
    }

    public TableColumnBuilder hide() {
        column.setMinWidth(0);
        column.setMaxWidth(0);
        return this;
    }

    public TableColumnBuilder headerValue(String headerValue) {
        column.setHeaderValue(headerValue);
        return this;
    }

    public TableColumnBuilder headerCenter() {
        return headerAlignment(SwingConstants.CENTER);
    }

    public TableColumnBuilder headerRigth() {
        return headerAlignment(SwingConstants.RIGHT);
    }

    private TableColumnBuilder headerAlignment(int horizontalAlignment) {
        column.setHeaderRenderer(new HorizontalAlignmentHeaderRenderer(horizontalAlignment));
        return this;
    }

    public TableColumnBuilder fixedWidth(int width) {
        column.setMinWidth(width);
        return width(width).maxWidth(width);
    }

    public TableColumnBuilder maxWidth(int width) {
        width(width);
        column.setMaxWidth(width);
        return this;
    }

    public TableColumnBuilder width(int width) {
        column.setPreferredWidth(width);
        return this;
    }

    public TableColumn getColumn() {
        return this.column;
    }
}
