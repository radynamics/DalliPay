package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LookupProviderException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LookupProviderFactory;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.AmountFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MoneyLabel extends JPanel {
    private final JLabel amt = new JLabel();
    private final JLabel detailLink;
    private Money value;
    private final Ledger ledger;

    public MoneyLabel(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
        detailLink = Utils.createLinkLabel(this, "show issuer...");
        detailLink.putClientProperty("FlatLaf.styleClass", "small");
        detailLink.setVisible(false);
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        {
            var c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            add(amt, c);
        }
        {
            detailLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (hasAmountIssuer() && e.getClickCount() == 1) {
                        lookup(value.getCcy().getIssuer());
                    }
                }
            });

            var c = new GridBagConstraints();
            c.insets = new Insets(0, 5, 0, 5);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 0.0;
            c.weighty = 0.5;
            c.gridx = 1;
            c.gridy = 0;
            add(detailLink, c);
        }
    }

    private void lookup(Wallet wallet) {
        try {
            var lp = LookupProviderFactory.createWalletLookupProvider(ledger);
            lp.open(wallet);
        } catch (LookupProviderException ex) {
            ExceptionDialog.show(this, ex);
        }
    }

    public void setAmount(Money value) {
        this.value = value;
        format();
    }

    private boolean hasAmountIssuer() {
        return value != null && value.getCcy().getIssuer() != null;
    }

    private void format() {
        detailLink.setVisible(hasAmountIssuer());
        if (value == null) {
            amt.setText("");
        }
        amt.setText(AmountFormatter.formatAmtWithCcy(value));
        var ccyFormatter = new CurrencyFormatter(ledger.getInfoProvider());
        ccyFormatter.format(amt, value.getCcy());
    }
}
