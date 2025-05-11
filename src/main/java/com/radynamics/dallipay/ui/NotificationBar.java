package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.concurrent.Callable;

public class NotificationBar extends JPanel {
    private final Map<JPanel, Set<Component>> _entryRigidAreas = new HashMap<>();

    public NotificationBar() {
        setBackground(Consts.ColorNotificationBar);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void addError(String text, String actionText, Callable<Void> action) {
        addEntry(text, actionText, action, false, ValidationState.Error);
    }

    public void addWarning(String text, String actionText, Callable<Void> action) {
        addEntry(text, actionText, action, false, ValidationState.Warning);
    }

    public void addInfo(String text, String actionText, Callable<Void> action) {
        addInfo(text, actionText, action, false);
    }

    public void addInfo(String text, String actionText, Callable<Void> action, boolean removeActionLinkAfterClick) {
        addEntry(text, actionText, action, removeActionLinkAfterClick, ValidationState.Info);
    }

    public void addEntry(String text, String actionText, Callable<Void> action, boolean removeActionLinkAfterClick, ValidationState severity) {
        var pnl = new JPanel();
        {
            var area0 = Box.createRigidArea(new Dimension(0, 3));
            add(area0);
            add(pnl);
            var area1 = Box.createRigidArea(new Dimension(0, 3));
            add(area1);

            _entryRigidAreas.put(pnl, new HashSet<>(Arrays.asList(area0, area1)));
        }

        pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setBackground(null);

        var margin = 5;
        {
            var lbl = new JLabel();
            pnl.add(lbl);
            lbl.setIcon(getIcon(severity));
        }
        pnl.add(Box.createRigidArea(new Dimension(margin, 0)));
        {
            pnl.add(new JLabel(text));
        }
        pnl.add(Box.createRigidArea(new Dimension(margin, 0)));
        {
            var lbl = Utils.createLinkLabel(this, actionText);
            pnl.add(lbl);
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        try {
                            if (removeActionLinkAfterClick) {
                                removeEntry(pnl);
                            }
                            action.call();
                        } catch (Exception ex) {
                            ExceptionDialog.show(pnl, ex);
                        }
                    }
                }
            });
        }
        pnl.add(Box.createHorizontalGlue());
        {
            var lbl = new JLabel();
            pnl.add(lbl);
            lbl.setIcon(new FlatSVGIcon("svg/close.svg", 16, 16));
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        removeEntry(pnl);
                    }
                }
            });
        }
    }

    private void removeEntry(JPanel pnl) {
        remove(pnl);
        // Also remove margin boxes.
        for (var c : _entryRigidAreas.get(pnl)) {
            remove(c);
        }
        _entryRigidAreas.remove(pnl);
        revalidate();
    }

    private Icon getIcon(ValidationState severity) {
        if (severity == ValidationState.Error) return new FlatSVGIcon("svg/error.svg", 16, 16);
        if (severity == ValidationState.Warning) return new FlatSVGIcon("svg/warning.svg", 16, 16);
        if (severity == ValidationState.Info) return new FlatSVGIcon("svg/info.svg", 16, 16);
        return new FlatSVGIcon("svg/info.svg", 16, 16);
    }
}
