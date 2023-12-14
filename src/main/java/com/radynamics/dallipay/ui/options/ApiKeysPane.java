package com.radynamics.dallipay.ui.options;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.db.ConfigRepo;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;

public class ApiKeysPane extends JPanel {
    private final SpringLayout contentLayout;
    private final JTextField txtXummApiKey;
    private final JTextField txtCryptoPriceOracleApiKey;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Options");

    public ApiKeysPane() {
        setPreferredSize(new Dimension(1000, 400));
        contentLayout = new SpringLayout();
        setLayout(contentLayout);

        txtXummApiKey = new JTextField();
        txtXummApiKey.setPreferredSize(new Dimension(330, 24));
        txtCryptoPriceOracleApiKey = new JTextField();
        txtCryptoPriceOracleApiKey.setPreferredSize(new Dimension(330, 24));
    }

    private void createUiControls(LedgerId ledgerId) {
        removeAll();
        var builder = new RowContentBuilder(this, contentLayout);
        final var topOffset = 5;
        var top = topOffset;
        {
            builder.addRowLabel(top, res.getString("restartNeeded"));
            top += 30;
        }
        {
            if (new HashSet<>(Arrays.asList(LedgerId.Xrpl, LedgerId.Xahau)).contains(ledgerId)) {
                builder.addRowLabel(top, res.getString("xummApi"));
                builder.addRowContent(top, txtXummApiKey);
                top += 30;
            }
        }
        {
            builder.addRowLabel(top, res.getString("cryptoPriceOracleApi"));
            builder.addRowContent(top, txtCryptoPriceOracleApiKey);
            top += 30;
        }
    }

    public void save(ConfigRepo repo) throws Exception {
        repo.setApiKeyXumm(txtXummApiKey.getText());
        repo.setApiKeyCryptoPriceOracle(txtCryptoPriceOracleApiKey.getText());
    }

    public void load(ConfigRepo repo) throws Exception {
        txtXummApiKey.setText(repo.getApiKeyXumm().orElse(null));
        txtCryptoPriceOracleApiKey.setText(repo.getApiKeyCryptoPriceOracle().orElse(null));
    }

    public void init(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        createUiControls(ledger.getId());
    }
}
