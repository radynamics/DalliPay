package com.radynamics.dallipay.ui.options;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.ui.WalletField;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class SenderPane extends JPanel {
    private Ledger ledger;
    private final SpringLayout contentLayout;
    private final JLabel lblDefaultSenderWallet;
    private final WalletField txtDefaultSenderWallet;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Options");

    public SenderPane() {
        setPreferredSize(new Dimension(1000, 400));
        contentLayout = new SpringLayout();
        setLayout(contentLayout);

        var builder = new RowContentBuilder(this, contentLayout);
        {
            final var topOffset = 5;
            var top = topOffset;
            {
                lblDefaultSenderWallet = builder.addRowLabel(top, "");
                txtDefaultSenderWallet = new WalletField(this);
                txtDefaultSenderWallet.setPreferredSize(new Dimension(330, 24));
                builder.addRowContent(top, txtDefaultSenderWallet);
                top += 30;
            }
        }
    }

    public void save(ConfigRepo repo) throws Exception {
        var defaultSenderWalletText = txtDefaultSenderWallet.getText();
        if (defaultSenderWalletText.length() == 0) {
            repo.setDefaultSenderWallet(ledger.getId(), null);
        } else if (ledger.isValidWallet(defaultSenderWalletText)) {
            repo.setDefaultSenderWallet(ledger.getId(), ledger.createWallet(defaultSenderWalletText, null));
        }
    }

    public void load(ConfigRepo repo) throws Exception {
        var defaultSenderWallet = repo.getDefaultSenderWallet(ledger);
        txtDefaultSenderWallet.setText(defaultSenderWallet == null ? "" : defaultSenderWallet.getPublicKey());
    }

    public void init(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;

        lblDefaultSenderWallet.setText(res.getString("defaultSenderWallet"));
        lblDefaultSenderWallet.setToolTipText(ledger.getDisplayText());
        txtDefaultSenderWallet.setLedger(this.ledger);
    }
}
