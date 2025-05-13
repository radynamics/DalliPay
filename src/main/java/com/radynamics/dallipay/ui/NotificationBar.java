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
    private final HashSet<NotificationBarEntry> _notifications = new HashSet<>();
    private final Map<NotificationBarEntry, Set<Component>> _entryRigidAreas = new HashMap<>();

    public NotificationBar() {
        setBackground(Consts.ColorNotificationBar);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public NotificationBarEntry addError(String text, String actionText, Callable<Void> action) {
        return addEntry(text, actionText, action, false, ValidationState.Error);
    }

    public NotificationBarEntry addWarning(String text, String actionText, Callable<Void> action) {
        return addEntry(text, actionText, action, false, ValidationState.Warning);
    }

    public NotificationBarEntry addInfo(String text, String actionText, Callable<Void> action) {
        return addInfo(text, actionText, action, false);
    }

    public NotificationBarEntry addInfo(String text, String actionText, Callable<Void> action, boolean removeActionLinkAfterClick) {
        return addEntry(text, actionText, action, removeActionLinkAfterClick, ValidationState.Info);
    }

    public NotificationBarEntry addEntry(String text, String actionText, Callable<Void> action, boolean removeActionLinkAfterClick, ValidationState severity) {
        var pnl = new JPanel();
        var notification = new NotificationBarEntry(pnl);
        {
            var area0 = Box.createRigidArea(new Dimension(0, 3));
            add(area0);
            add(pnl);
            var area1 = Box.createRigidArea(new Dimension(0, 3));
            add(area1);

            _entryRigidAreas.put(notification, new HashSet<>(Arrays.asList(area0, area1)));
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
                                removeEntry(notification);
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
                        removeEntry(notification);
                    }
                }
            });
        }

        _notifications.add(notification);
        return notification;
    }


    public void removeEntry(NotificationBarEntry notification) {
        var pnl = notification.getPanel();
        remove(pnl);

        // Also remove margin boxes.
        for (var c : _entryRigidAreas.get(notification)) {
            remove(c);
        }
        _entryRigidAreas.remove(pnl);
        revalidate();

        _notifications.remove(notification);
    }

    private Icon getIcon(ValidationState severity) {
        if (severity == ValidationState.Error) return new FlatSVGIcon("svg/error.svg", 16, 16);
        if (severity == ValidationState.Warning) return new FlatSVGIcon("svg/warning.svg", 16, 16);
        if (severity == ValidationState.Info) return new FlatSVGIcon("svg/info.svg", 16, 16);
        return new FlatSVGIcon("svg/info.svg", 16, 16);
    }
}
