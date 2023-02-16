package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.InfoType;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
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
    private Ledger ledger;
    private WalletFieldInputValidator validator;
    private ValidationControlDecorator decorator;

    public WalletField(JComponent owner) {
        this.owner = owner;
        setupUI();
        setShowDetailVisible(false);
    }

    private void setupUI() {
        setLayout(new GridBagLayout());

        {
            txt = new JTextField();
            txt.setColumns(25);
            txt.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    var text = ((JTextField) input).getText();
                    decorator.update(text);
                    return validator.getValidOrNull(text) != null;
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
            pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));

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
        var wallet = ledger.createWallet(getText(), "");
        if (!WalletValidator.isValidFormat(ledger, wallet)) {
            return;
        }

        ledger.refreshBalance(wallet, true);

        var pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));

        {
            var lbl = new JLabel(wallet.getPublicKey());
            lbl.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
            pnl.add(lbl);
        }

        var sb = new StringBuilder();
        sb.append("=== Balances ===" + System.lineSeparator());
        sb.append(MoneyFormatter.formatFiat(Money.sort(wallet.getBalances().all()), System.lineSeparator()));
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
            if (wi.getType() == InfoType.Url) {
                var lbl = Utils.createLinkLabel(pnl, wi.getText() + "...");
                if (isFirstUrl) {
                    lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                    isFirstUrl = false;
                }
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 1) {
                            Utils.openBrowser(pnl, URI.create(wi.getValue()));
                        }
                    }
                });
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
}
