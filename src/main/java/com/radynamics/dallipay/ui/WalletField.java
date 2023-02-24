package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.xrpl.walletinfo.InfoType;
import com.radynamics.dallipay.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;
import com.radynamics.dallipay.exchange.Money;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

public class WalletField extends JPanel {
    private final static Logger log = LogManager.getLogger(WalletField.class);
    private final JComponent owner;
    private JTextField txt;
    private JLabel lblShowDetail;
    private JLabel lblInfoText;
    private Ledger ledger;
    private WalletFieldInputValidator validator;
    private ValidationControlDecorator decorator;

    public WalletField(JComponent owner) {
        this(owner, true);
    }

    public WalletField(JComponent owner, boolean verticalLabels) {
        this.owner = owner;
        setupUI(verticalLabels);
        setShowDetailVisible(false);
        setInfoTextVisible(false);
    }

    private void setupUI(boolean verticalLabels) {
        setLayout(new GridBagLayout());

        {
            txt = new JTextField();
            txt.setColumns(25);
            txt.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    var text = ((JTextField) input).getText();
                    decorator.update(text);
                    updateInfoText(text);
                    return validator.isValid(text);
                }
            });
            txt.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    processFocusEvent(e);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    processFocusEvent(e);
                }
            });
            var c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            add(txt, c);
        }
        {
            var pnl = new JPanel();
            pnl.setLayout(new BoxLayout(pnl, verticalLabels ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));

            var c = new GridBagConstraints();
            c.insets = new Insets(0, 5, 0, 5);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 0.0;
            c.weighty = 0.5;
            c.gridx = 1;
            c.gridy = 0;
            add(pnl, c);
            {
                var lbl = Utils.createLinkLabel(owner, "find...");
                if (!verticalLabels) {
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
                }
                pnl.add(lbl);
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        lookup();
                    }
                });
            }
            {
                lblShowDetail = Utils.createLinkLabel(owner, "detail...");
                pnl.add(lblShowDetail);
                lblShowDetail.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showMore();
                    }
                });
            }
        }
        {
            lblInfoText = Utils.formatSecondaryInfo(new JLabel());

            var c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 0.5;
            c.gridx = 0;
            c.gridy = 1;
            add(lblInfoText, c);
        }
    }

    private void updateInfoText(String text) {
        var wallet = validator.getValidOrNull(text);
        if (wallet == null) {
            lblInfoText.setText("Invalid wallet");
            return;
        }

        var aggregator = new WalletInfoAggregator(ledger.getInfoProvider());
        var wi = aggregator.getNameOrDomain(wallet);
        lblInfoText.setText(WalletInfoFormatter.toText(wi).orElse("No information available"));
        WalletInfoFormatter.format(lblInfoText, wi);
    }

    private void lookup() {
        try {
            var wallet = ledger.createWallet(getText(), "");
            var lp = LookupProviderFactory.createWalletLookupProvider(ledger);
            lp.open(wallet);
        } catch (LookupProviderException ex) {
            ExceptionDialog.show(this, ex);
        }
    }

    private void showMore() {
        var wallet = getValidWallet();
        if (wallet == null) {
            return;
        }

        var pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));

        {
            var lbl = new JLabel(wallet.getPublicKey());
            lbl.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
            pnl.add(lbl);
        }

        var sb = new StringBuilder();
        sb.append("=== Balances ===" + System.lineSeparator());
        sb.append(MoneyFormatter.formatFiat(Money.sort(Money.removeZero(wallet.getBalances().all())), System.lineSeparator()));
        sb.append(System.lineSeparator());

        var infos = new ArrayList<WalletInfo>();
        for (var p : ledger.getInfoProvider()) {
            try {
                infos.addAll(Arrays.asList(p.list(wallet)));
            } catch (WalletInfoLookupException e) {
                log.warn(e.getMessage(), e);
            }
        }

        String lastInfoProviderDisplayText = null;
        boolean isFirstUrl = true;
        for (var wi : infos) {
            var infoProviderDisplayText = wi.getProvider().getDisplayText();
            if (!StringUtils.equals(lastInfoProviderDisplayText, infoProviderDisplayText)) {
                if (sb.length() > 0) {
                    sb.append(System.lineSeparator());
                }
                sb.append(String.format("=== %s ===%s", infoProviderDisplayText, System.lineSeparator()));
            }
            if (wi.getType() == InfoType.Url || wi.getType() == InfoType.Domain) {
                var lbl = createLinkLabel(wi);
                if (isFirstUrl) {
                    lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                    isFirstUrl = false;
                }
                pnl.add(lbl);
            } else {
                var text = StringUtils.isEmpty(wi.getText()) ? "" : String.format("%s: ", wi.getText());
                sb.append(String.format("%s%s%s", text, wi.getValue(), System.lineSeparator()));
            }
            lastInfoProviderDisplayText = infoProviderDisplayText;
        }

        // ScrollPane trick to force a top margin
        pnl.add(new JLabel(" "));

        var textArea = new JTextArea(sb.toString());
        textArea.setColumns(50);
        textArea.setRows(15);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
        pnl.add(new JScrollPane(textArea));
        JOptionPane.showMessageDialog(this, pnl, wallet.getPublicKey(), JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel createLinkLabel(WalletInfo wi) {
        var text = wi.getText();
        switch (wi.getType()) {
            case Domain -> {
                text = wi.getValue();
                if (!wi.getVerified()) {
                    text += " (unverified)";
                }
            }
        }
        var lbl = Utils.createLinkLabel(owner, text + "...");
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    var url = wi.getType() == InfoType.Domain
                            ? "https://www." + wi.getValue()
                            : wi.getValue();
                    Utils.openBrowser(owner, URI.create(url));
                }
            }
        });
        return lbl;
    }

    public void setText(String value) {
        txt.setText(value);
        txt.getInputVerifier().verify(txt);
    }

    public String getText() {
        return txt.getText().trim();
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
        validator = new WalletFieldInputValidator(ledger);
        decorator = new ValidationControlDecorator(txt, validator);
    }

    public void setEditable(boolean b) {
        txt.setEditable(b);
    }

    public void setShowDetailVisible(boolean visible) {
        lblShowDetail.setVisible(visible);
    }

    public void setInfoTextVisible(boolean visible) {
        lblInfoText.setVisible(visible);
    }

    public void setWallet(Wallet wallet) {
        if (wallet == null) {
            setText("");
        } else {
            setText(wallet.getPublicKey());
        }
    }

    public Wallet getValidWallet() {
        var wallet = validator.getValidOrNull(getText());
        if (wallet == null) {
            return null;
        }

        ledger.refreshBalance(wallet, true);
        return wallet;
    }

    public Wallet getWallet() {
        var valid = getValidWallet();
        if (valid != null) {
            return valid;
        }

        return getText().length() == 0 ? null : ledger.createWallet(txt.getText(), null);
    }
}
