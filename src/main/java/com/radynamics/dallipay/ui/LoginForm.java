package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.db.Database;
import com.radynamics.dallipay.util.RequestFocusListener;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class LoginForm {
    private JPasswordField pf = new JPasswordField(20);

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public boolean showLogin() {
        return showLogin(res.getString("enterPassword"), "Login DalliPay");
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
        if (!show(res.getString("newPassword"), res.getString("newPasswordTitle"))) {
            return false;
        }

        if (!Database.isPasswordAcceptable(getPassword())) {
            JOptionPane.showMessageDialog(parentComponent, res.getString("mustEnterPassword"), "DalliPay", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean show(String labelText, String title) {
        var frm = new JFrame("DalliPay");
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
        pf.select(0, pf.getPassword().length);
        pf.addAncestorListener(new RequestFocusListener());
        var result = JOptionPane.showOptionDialog(null, pnl, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon, null, null);
        frm.dispose();
        return JOptionPane.OK_OPTION == result;
    }

    public String getPassword() {
        return new String(pf.getPassword());
    }
}
