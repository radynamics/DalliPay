package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LookupProviderException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LookupProviderFactory;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletValidator;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WalletField extends JPanel {
    private final JComponent owner;
    private JTextField txt;
    private JLabel lblShowDetail;
    private Ledger ledger;

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
                    var result = StringUtils.isEmpty(text) ? null : new WalletValidator(ledger).validateFormat(ledger.createWallet(text, null));
                    if (result == null) {
                        txt.putClientProperty("JComponent.outline", null);
                        return true;
                    } else {
                        txt.putClientProperty("JComponent.outline", "error");
                        return false;
                    }
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

        try {
            String lastInfoProviderDisplayText = null;
            var sb = new StringBuilder();
            for (var p : ledger.getInfoProvider()) {
                for (var wi : p.list(wallet)) {
                    var infoProviderDisplayText = wi.getProvider().getDisplayText();
                    if (!StringUtils.equals(lastInfoProviderDisplayText, infoProviderDisplayText)) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(String.format("=== %s ===\n", infoProviderDisplayText));
                    }
                    sb.append(String.format("%s: %s\n", wi.getText(), wi.getValue()));
                    lastInfoProviderDisplayText = infoProviderDisplayText;
                }
            }

            var textArea = new JTextArea(sb.toString());
            textArea.setColumns(30);
            textArea.setRows(15);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), wallet.getPublicKey(), JOptionPane.INFORMATION_MESSAGE);
        } catch (WalletInfoLookupException e) {
            ExceptionDialog.show(this, e);
        }
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
    }

    public void setEditable(boolean b) {
        txt.setEditable(b);
    }

    public void setShowDetailVisible(boolean visible) {
        lblShowDetail.setVisible(visible);
    }
}
