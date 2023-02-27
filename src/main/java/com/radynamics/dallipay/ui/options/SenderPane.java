package com.radynamics.dallipay.ui.options;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.ui.WalletField;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class SenderPane extends JPanel {
    private final Ledger ledger;
    private final SpringLayout contentLayout;
    private final WalletField txtDefaultSenderWallet;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Options");

    public SenderPane(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;

        setPreferredSize(new Dimension(1000, 400));
        contentLayout = new SpringLayout();
        setLayout(contentLayout);

        var builder = new RowContentBuilder(this, contentLayout);
        {
            final var topOffset = 5;
            var top = topOffset;
            {
                builder.addRowLabel(top, String.format(res.getString("defaultSenderWallet"), ledger.getId().textId().toUpperCase(Locale.ROOT)));
                txtDefaultSenderWallet = new WalletField(this);
                txtDefaultSenderWallet.setLedger(this.ledger);
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
        } else if (ledger.isValidPublicKey(defaultSenderWalletText)) {
            repo.setDefaultSenderWallet(ledger.getId(), ledger.createWallet(defaultSenderWalletText, null));
        }
    }

    public void load(ConfigRepo repo) throws Exception {
        var defaultSenderWallet = repo.getDefaultSenderWallet(ledger);
        txtDefaultSenderWallet.setText(defaultSenderWallet == null ? "" : defaultSenderWallet.getPublicKey());
    }
}
