package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletLookupProvider;
import com.radynamics.CryptoIso20022Interop.ui.Consts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WalletField extends JPanel {
    private WalletLookupProvider lookupProvider;
    private JTextField txt;

    public WalletField(WalletLookupProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());

        {
            txt = new JTextField();
            var c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            add(txt, c);
        }
        {
            var lbl = new JLabel("find...");
            lbl.setForeground(Consts.ColorAccent);
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    lookup();
                }
            });

            var c = new GridBagConstraints();
            c.insets = new Insets(0, 5, 0, 5);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 0.0;
            c.weighty = 0.5;
            c.gridx = 1;
            c.gridy = 0;
            add(lbl, c);
        }
    }

    private void lookup() {
        lookupProvider.open(getText());
    }

    public void setText(String value) {
        txt.setText(value);
    }

    public String getText() {
        return txt.getText();
    }
}
