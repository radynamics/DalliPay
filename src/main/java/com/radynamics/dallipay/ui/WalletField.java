package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.common.primitives.UnsignedInteger;
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
import java.util.ResourceBundle;

public class WalletField extends JPanel {
    private final static Logger log = LogManager.getLogger(WalletField.class);
    private final JComponent owner;
    private JTextField txt;
    private JTextField destinationTag;
    private JToggleButton detailButton;
    private JLabel lblInfoText;
    private Ledger ledger;
    private WalletFieldInputValidator walletValidator;
    private ValidationControlDecorator walletDecorator;
    private DestinationTagInputValidator destinationTagValidator;
    private ValidationControlDecorator destinationTagDecorator;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public WalletField(JComponent owner) {
        this.owner = owner;
        setupUI();
        setDestinationTagVisible(false);
        setShowDetailVisible(false);
        setInfoTextVisible(false);
    }

    private void setupUI() {
        setLayout(new GridBagLayout());

        {
            txt = new JTextField();
            txt.setColumns(28);
            txt.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("placeholderText"));
            txt.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, createToolbar());
            txt.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    var text = ((JTextField) input).getText();
                    walletDecorator.update(text);
                    updateInfoText(text);
                    return walletValidator.isValid(text);
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
            destinationTag = new JTextField();
            destinationTag.setColumns(6);
            destinationTag.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("destinationTag.placeholderText"));
            destinationTag.setToolTipText(res.getString("destinationTag.tooltipText"));
            destinationTag.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    var text = ((JTextField) input).getText();
                    destinationTagDecorator.update(text);
                    return destinationTagValidator.isValid(text);
                }
            });

            var c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 0.5;
            c.gridx = 1;
            c.gridy = 0;
            add(destinationTag, c);
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

    private JToolBar createToolbar() {
        var toolbar = new JToolBar();
        {
            var cmd = new JToggleButton(new FlatSVGIcon("svg/search.svg", 16, 16));
            toolbar.add(cmd);
            Utils.setRolloverIcon(cmd);
            cmd.setToolTipText(res.getString("find"));
            cmd.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    lookup();
                }
            });
        }
        {
            detailButton = new JToggleButton(new FlatSVGIcon("svg/listul.svg", 16, 16));
            toolbar.add(detailButton);
            Utils.setRolloverIcon(detailButton);
            detailButton.setToolTipText(res.getString("detail"));
            detailButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showMore();
                }
            });
        }

        return toolbar;
    }

    private void updateInfoText(String text) {
        var wallet = walletValidator.getValidOrNull(text);
        if (wallet == null && text.length() > 0) {
            lblInfoText.setText(res.getString("invalidWallet"));
            return;
        }

        var aggregator = new WalletInfoAggregator(ledger.getInfoProvider());
        var wi = aggregator.getNameOrDomain(wallet);
        if (text.length() > 0) {
            lblInfoText.setText(WalletInfoFormatter.toText(wi).orElse(res.getString("noInfo")));
        } else {
            lblInfoText.setText("");
        }
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
        sb.append("=== " + res.getString("balances") + " ===" + System.lineSeparator());
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
                    text += " " + res.getString("unverified");
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
        walletValidator = new WalletFieldInputValidator(ledger);
        walletDecorator = new ValidationControlDecorator(txt, walletValidator);
        destinationTagValidator = new DestinationTagInputValidator(ledger.createDestinationTagBuilder());
        destinationTagDecorator = new ValidationControlDecorator(destinationTag, destinationTagValidator);
    }

    public void setEditable(boolean b) {
        txt.setEditable(b);
    }

    public void setDestinationTagVisible(boolean visible) {
        destinationTag.setVisible(visible);
    }

    public void setShowDetailVisible(boolean visible) {
        detailButton.setVisible(visible);
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
        var wallet = walletValidator.getValidOrNull(getText());
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

    public UnsignedInteger getDestinationTag() {
        return destinationTagValidator.getValidOrNull(destinationTag.getText().trim());
    }

    public void setDestinationTag(UnsignedInteger destinationTag) {
        this.destinationTag.setText(destinationTag == null ? "" : String.valueOf(destinationTag));
    }
}
