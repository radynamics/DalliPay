package com.radynamics.dallipay.ui.options;

import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.VersionController;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LookupProviderFactory;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.db.Database;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.ui.ExceptionDialog;
import com.radynamics.dallipay.ui.LoginForm;
import com.radynamics.dallipay.ui.Utils;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ResourceBundle;

public class GeneralPane extends JPanel {
    private final static Logger log = LogManager.getLogger(GeneralPane.class);
    private HttpUrl faucetUrl;

    private final SpringLayout contentLayout;
    private Ledger ledger;
    private JComboBox<String> cboExplorer;
    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Options");
    private final JButton cmdWalletSetup = new JButton(res.getString("start"));

    public GeneralPane() {
        setPreferredSize(new Dimension(1000, 400));
        contentLayout = new SpringLayout();
        setLayout(contentLayout);

    }

    private void createUiControls() {
        removeAll();
        var builder = new RowContentBuilder(this, contentLayout);
        {
            final var topOffset = 5;
            var top = topOffset;
            {
                builder.addRowLabel(top, res.getString("explorer"));
                cboExplorer = new JComboBox<>();
                cboExplorer.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        return super.getListCellRendererComponent(list, LookupProviderFactory.getDisplayText(value.toString()), index, isSelected, cellHasFocus);
                    }
                });
                cboExplorer.setPreferredSize(new Dimension(150, 21));
                builder.addRowContent(top, cboExplorer);
                top += 30;
            }
            {
                builder.addRowLabel(top, res.getString("version"));
                var pnl = new JPanel();
                {
                    var vc = new VersionController();
                    pnl.add(new JLabel(vc.getVersion()));
                }
                {
                    var lbl = Utils.createLinkLabel(this, res.getString("showLicenses"));
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
                builder.addRowLabel(top, res.getString("website"));
                var lbl = Utils.createLinkLabel(this, "www.dallipay.com");
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Utils.openBrowser(lbl, URI.create("https://www.dallipay.com"));
                    }
                });
                builder.addRowContent(top, lbl);
                top += 30;
            }
            {
                builder.addRowLabel(top, res.getString("dbPassword"));
                var cmd = new JButton(res.getString("change"));
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> onChangePassword());
                builder.addRowContent(top, cmd);
                top += 50;
            }
            {
                builder.addRowLabel(top, res.getString("testWallet"));
                var cmd = new JButton(res.getString("create"));
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> onCreateTestWallet());
                builder.addRowContent(top, cmd);
                top += 50;
            }
            if (ledger.createWalletSetupProcess(this) != null) {
                builder.addRowLabel(top, res.getString("walletSetup"));
                cmdWalletSetup.setPreferredSize(new Dimension(150, 35));
                cmdWalletSetup.addActionListener(e -> onSetupWallet());
                builder.addRowContent(top, cmdWalletSetup);
                top += 50;
            }
        }
    }

    private void refreshExplorer() {
        cboExplorer.removeAllItems();
        for (var id : LookupProviderFactory.allIds(ledger.getId())) {
            cboExplorer.addItem(id);
        }
    }

    private void showLicenses() {
        var sb = new StringBuilder();
        sb.append("Jaxb RI, Copyright (c) 2018 Oracle and/or its affiliates, https://github.com/eclipse-ee4j/jersey" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Eclipse Jersey Project, Eclipse Public License - v 2.0, https://github.com/eclipse-ee4j/jersey" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("org.json, Copyright (c) 2002 JSON.org, https://github.com/stleary/JSON-java" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Apache Commons, Apache License 2.0, https://commons.apache.org" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Apache Log4j 2, Apache License 2.0, https://logging.apache.org/log4j/2.x/index.html" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("OkHttp, Apache License 2.0, https://github.com/square/okhttp" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("xrpl4j, Copyright (c) 2020, XRP Ledger Foundation, https://github.com/XRPLF/xrpl4j" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("FlatLaf, Apache License 2.0, https://github.com/JFormDesigner/FlatLaf" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("LGoodDatePicker, MIT License, https://github.com/LGoodDatePicker/LGoodDatePicker" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("JSplitButton, Apache License 2.0, https://github.com/rhwood/jsplitbutton" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("SQLite JDBC Driver, Apache License 2.0, https://github.com/Willena/sqlite-jdbc-crypt" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("toml4j, MIT License, https://github.com/mwanji/toml4j" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Semver4j, Copyright (c) 2015-present Vincent DURMONT, https://github.com/vdurmont/semver4j" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("JUnit, Eclipse Public License - v 2.0, https://junit.org" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("XMLUnit, Apache License 2.0, https://www.xmlunit.org" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Opencsv, Apache License 2.0, https://opencsv.sourceforge.net" + System.lineSeparator());

        var textArea = new JTextArea(sb.toString());
        textArea.setColumns(30);
        textArea.setRows(15);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), res.getString("usedlibraries"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void onChangePassword() {
        try {
            var frm = new LoginForm();
            if (!frm.showLogin(res.getString("currentPassword"), res.getString("changePassword"))) {
                return;
            }
            if (!Database.isReadable(frm.getPassword())) {
                JOptionPane.showMessageDialog(this, res.getString("invalidPassword"));
                return;
            }

            frm = new LoginForm();
            if (!frm.showNewPassword(this)) {
                return;
            }
            Database.changePassword(frm.getPassword());
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
    }

    public void onCreateTestWallet() {
        if (ledger.getNetwork().isLivenet()) {
            JOptionPane.showMessageDialog(this, String.format(res.getString("createTestWalletFailed"), ledger.getNetwork().getShortText()), ledger.getNetwork().getShortText(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (faucetUrl == null) {
            JOptionPane.showMessageDialog(this, String.format(res.getString("faucetNotAvailable"), ledger.getNetwork().getDisplayName()), ledger.getNetwork().getDisplayName(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        var wallet = ledger.createRandomWallet(faucetUrl);

        var sb = new StringBuilder();
        sb.append("=== Wallet ===" + System.lineSeparator());
        sb.append(String.format("Wallet: %s", wallet.getPublicKey()) + System.lineSeparator());
        sb.append(String.format("%s: %s", res.getString("secret"), wallet.getSecret()) + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append(String.format("Faucet: %s", faucetUrl) + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("=== " + res.getString("balances") + " ===" + System.lineSeparator());
        sb.append(MoneyFormatter.formatFiat(Money.sort(wallet.getBalances().all()), System.lineSeparator()));

        var txt = new JTextArea(sb.toString());
        txt.setColumns(30);
        txt.setRows(10);
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setSize(txt.getPreferredSize().width, txt.getPreferredSize().height);
        JOptionPane.showMessageDialog(this, new JScrollPane(txt), res.getString("testWalletCreated"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void onSetupWallet() {
        var p = ledger.createWalletSetupProcess(this);
        if (p == null) {
            return;
        }

        try {
            p.start();
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
    }

    public void save(ConfigRepo repo) throws Exception {
        repo.setLookupProviderId(ledger.getId(), cboExplorer.getSelectedItem().toString());
    }

    public void load(ConfigRepo repo) throws Exception {
        faucetUrl = repo.getFaucetUrl(ledger);
        cboExplorer.setSelectedItem(repo.getLookupProviderId(ledger.getId()).orElse(ledger.getDefaultLookupProviderId()));
    }

    public void init(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
        createUiControls();
        refreshExplorer();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        cmdWalletSetup.setEnabled(enabled);
    }
}
