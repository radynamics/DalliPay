package com.radynamics.CryptoIso20022Interop.ui.options;

import com.radynamics.CryptoIso20022Interop.VersionController;
import com.radynamics.CryptoIso20022Interop.db.Database;
import com.radynamics.CryptoIso20022Interop.ui.ExceptionDialog;
import com.radynamics.CryptoIso20022Interop.ui.LoginForm;

import javax.swing.*;
import java.awt.*;

public class GeneralPane extends JPanel {
    private final SpringLayout contentLayout;

    public GeneralPane() {
        setPreferredSize(new Dimension(1000, 400));
        contentLayout = new SpringLayout();
        setLayout(contentLayout);

        var builder = new RowContentBuilder(this, contentLayout);
        {
            final var topOffset = 5;
            var top = topOffset;
            {
                builder.addRowLabel(top, "Version:");
                var vc = new VersionController();
                builder.addRowContent(top, new JLabel(vc.getVersion()));
                top += 30;
            }
            {
                builder.addRowLabel(top, "Database password");
                var cmd = new JButton("change...");
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> onChangePassword());
                builder.addRowContent(top, cmd);
                top += 30;
            }
        }
    }

    private void onChangePassword() {
        try {
            var frm = new LoginForm();
            if (!frm.showLogin("Current password", "Change password")) {
                return;
            }
            if (!Database.isReadable(frm.getPassword())) {
                JOptionPane.showMessageDialog(this, "Invalid password");
                return;
            }

            frm = new LoginForm();
            if (!frm.showNewPassword()) {
                return;
            }
            var newPassword = frm.getPassword();
            if (!Database.isPasswordAcceptable(newPassword)) {
                JOptionPane.showMessageDialog(this, "New password cannot be empty.");
                return;
            }
            Database.changePassword(newPassword);
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
    }

    public void save() {
        // do nothing
    }

    public void load() {
        // do nothing
    }
}
