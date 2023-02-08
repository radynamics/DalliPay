package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.db.Database;
import com.radynamics.CryptoIso20022Interop.util.RequestFocusListener;

import javax.swing.*;
import java.awt.*;

public class LoginForm {
    private JPasswordField pf = new JPasswordField(20);

    public boolean showLogin() {
        return showLogin("Enter password", "Login CryptoIso20022Interop");
    }

    public boolean showLogin(String labelText, String title) {
        if (!show(labelText + ":", title)) {
            return false;
        }

        if (Database.isReadable(getPassword())) {
            return true;
        }

        return showLogin();
    }

    public boolean showNewPassword(Component parentComponent) {
        if (!show("New password:", "Enter new password")) {
            return false;
        }

        if (!Database.isPasswordAcceptable(getPassword())) {
            JOptionPane.showMessageDialog(parentComponent, "You must define a password to continue.", "CryptoIso20022 Interop", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean show(String labelText, String title) {
        var frm = new JFrame("CryptoIso20022Interop");
        frm.setIconImage(Utils.getProductIcon());
        frm.setUndecorated(true);
        frm.setVisible(true);
        frm.setLocationRelativeTo(null);

        var pnl = new JPanel();
        pnl.setLayout(new GridLayout(2, 1));

        var lbl = new JLabel(labelText);
        pnl.add(lbl);
        pnl.add(pf);

        var icon = Utils.getScaled("img/productIcon.png", 64, 64);
        pf.addAncestorListener(new RequestFocusListener());
        var result = JOptionPane.showOptionDialog(null, pnl, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon, null, null);
        frm.dispose();
        return JOptionPane.OK_OPTION == result;
    }

    public String getPassword() {
        return new String(pf.getPassword());
    }
}
