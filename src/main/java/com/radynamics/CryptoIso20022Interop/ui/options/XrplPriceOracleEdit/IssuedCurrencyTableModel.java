package com.radynamics.CryptoIso20022Interop.ui.options.XrplPriceOracleEdit;

import org.apache.commons.lang3.StringUtils;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IssuedCurrencyTableModel extends AbstractTableModel {
    private final String[] columnNames = {COL_FROM, COL_TO, COL_ISSUER, COL_RECEIVER};
    private List<Record> list;

    public static final String COL_FROM = "from";
    public static final String COL_TO = "to";
    public static final String COL_ISSUER = "issuer";
    public static final String COL_RECEIVER = "receiver";

    public void load(List<Record> data) {
        list = data;
        fireTableDataChanged();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return list == null ? 0 : list.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    private int getColumnIndex(String identifier) {
        return Arrays.asList(columnNames).indexOf(identifier);
    }

    public Object getValueAt(int row, int col) {
        var item = list.get(row);
        if (getColumnIndex(COL_FROM) == col) {
            return item.first;
        } else if (getColumnIndex(COL_TO) == col) {
            return item.second;
        } else if (getColumnIndex(COL_ISSUER) == col) {
            return item.issuer;
        } else if (getColumnIndex(COL_RECEIVER) == col) {
            return item.receiver;
        }
        return null;
    }

    public void setValueAt(Object value, int row, int col) {
        var item = list.get(row);
        if (getColumnIndex(COL_FROM) == col) {
            item.first = (String) value;
        } else if (getColumnIndex(COL_TO) == col) {
            item.second = (String) value;
        } else if (getColumnIndex(COL_ISSUER) == col) {
            item.issuer = (String) value;
        } else if (getColumnIndex(COL_RECEIVER) == col) {
            item.receiver = (String) value;
        }
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int column) {
        return true;
    }

    public List<Record> issuedCurrencies() {
        var list = new ArrayList<Record>();
        for (var item : this.list) {
            if (!item.isEmpty() && !StringUtils.isEmpty(item.first) && !StringUtils.isEmpty(item.second)) {
                list.add(item);
            }
        }
        return list;
    }

    public Record newRecord() {
        var o = new Record();
        list.add(o);
        int row = getRowIndex(o);
        fireTableRowsInserted(row, row);
        return o;
    }

    public int getRowIndex(Record o) {
        return list.indexOf(o);
    }

    public void remove(int row) {
        list.remove(row);
        fireTableRowsDeleted(row, row);
    }
}
