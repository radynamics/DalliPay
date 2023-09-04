package com.radynamics.dallipay.ui.paymentTable;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public final class TableCellMouseOverCursor {
    public static void set(JComponent owner, JTable table, String columnIdentifier) {
        table.addMouseMotionListener(new MouseMotionListener() {
            private boolean inside;

            @Override
            public void mouseDragged(MouseEvent e) {
                // do nothing
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // Cursor in column but below last row (empty area).
                if (table.rowAtPoint(e.getPoint()) == -1) {
                    showDefaultCursor();
                    return;
                }

                var column = table.getColumnModel().getColumn(table.columnAtPoint(e.getPoint()));
                if (StringUtils.equals((String) column.getIdentifier(), columnIdentifier)) {
                    showHandCursor();
                } else {
                    showDefaultCursor();
                }
            }

            private void showHandCursor() {
                if (inside) {
                    return;
                }
                inside = true;
                owner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            private void showDefaultCursor() {
                if (!inside) {
                    return;
                }
                inside = false;
                owner.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
}
