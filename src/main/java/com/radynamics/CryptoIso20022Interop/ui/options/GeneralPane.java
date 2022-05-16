package com.radynamics.CryptoIso20022Interop.ui.options;

import com.radynamics.CryptoIso20022Interop.VersionController;
import com.radynamics.CryptoIso20022Interop.db.Database;
import com.radynamics.CryptoIso20022Interop.ui.ExceptionDialog;
import com.radynamics.CryptoIso20022Interop.ui.LoginForm;
import com.radynamics.CryptoIso20022Interop.ui.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GeneralPane extends JPanel {
    private final SpringLayout contentLayout;

    public GeneralPane(Window owner) {
        setPreferredSize(new Dimension(1000, 400));
        contentLayout = new SpringLayout();
        setLayout(contentLayout);

        var builder = new RowContentBuilder(this, contentLayout);
        {
            final var topOffset = 5;
            var top = topOffset;
            {
                builder.addRowLabel(top, "Version:");
                var pnl = new JPanel();
                {
                    var vc = new VersionController();
                    pnl.add(new JLabel(vc.getVersion()));
                }
                {
                    var lbl = Utils.createLinkLabel(owner, "show licenses...");
                    lbl.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            showLicenses();
                        }
                    });
                    pnl.add(lbl);
                }
                builder.addRowContent(top, pnl);
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

    private void showLicenses() {
        var sb = new StringBuilder();
        sb.append("Jaxb RI, Copyright (c) 2018 Oracle and/or its affiliates, https://github.com/eclipse-ee4j/jersey\n");
        sb.append("\n");
        sb.append("Eclipse Jersey Project, Eclipse Public License - v 2.0, https://github.com/eclipse-ee4j/jersey\n");
        sb.append("\n");
        sb.append("org.json, Copyright (c) 2002 JSON.org, https://github.com/stleary/JSON-java\n");
        sb.append("\n");
        sb.append("Apache Commons, Apache License 2.0, https://commons.apache.org\n");
        sb.append("\n");
        sb.append("Apache Log4j 2, Apache License 2.0, https://logging.apache.org/log4j/2.x/index.html\n");
        sb.append("\n");
        sb.append("OkHttp, Apache License 2.0, https://github.com/square/okhttp\n");
        sb.append("\n");
        sb.append("xrpl4j, Copyright (c) 2020, XRP Ledger Foundation, https://github.com/XRPLF/xrpl4j\n");
        sb.append("\n");
        sb.append("FlatLaf, Apache License 2.0, https://github.com/JFormDesigner/FlatLaf\n");
        sb.append("\n");
        sb.append("LGoodDatePicker, MIT License, https://github.com/LGoodDatePicker/LGoodDatePicker\n");
        sb.append("\n");
        sb.append("JSplitButton, Apache License 2.0, https://github.com/rhwood/jsplitbutton\n");
        sb.append("\n");
        sb.append("SQLite JDBC Driver, Apache License 2.0, https://github.com/Willena/sqlite-jdbc-crypt\n");
        sb.append("\n");
        sb.append("toml4j, MIT License, https://github.com/mwanji/toml4j\n");
        sb.append("\n");
        sb.append("Semver4j, Copyright (c) 2015-present Vincent DURMONT, https://github.com/vdurmont/semver4j\n");
        sb.append("\n");
        sb.append("JUnit, Eclipse Public License - v 2.0, https://junit.org\n");
        sb.append("\n");
        sb.append("XMLUnit, Apache License 2.0, https://www.xmlunit.org\n");

        var textArea = new JTextArea(sb.toString());
        textArea.setColumns(30);
        textArea.setRows(15);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Used libraries", JOptionPane.INFORMATION_MESSAGE);
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
