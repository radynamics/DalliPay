package com.radynamics.dallipay.ui.options;

import com.radynamics.dallipay.db.ConfigRepo;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class ApiKeysPane extends JPanel {
    private final SpringLayout contentLayout;
    private final JTextField txtXummApiKey;
    private final JTextField txtApiKeyAlchemyEthereumMainnet;
    private final JTextField txtApiKeyAlchemyEthereumGoerli;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Options");

    public ApiKeysPane() {
        setPreferredSize(new Dimension(1000, 400));
        contentLayout = new SpringLayout();
        setLayout(contentLayout);

        var builder = new RowContentBuilder(this, contentLayout);
        {
            final var topOffset = 5;
            var top = topOffset;

            builder.addRowLabel(top, res.getString("restartNeeded"));
            txtXummApiKey = createTextField(builder, top += 30, res.getString("xummApi"));
            txtApiKeyAlchemyEthereumMainnet = createTextField(builder, top += 30, res.getString("apiKeyAlchemyEthereumMainnet"));
            txtApiKeyAlchemyEthereumGoerli = createTextField(builder, top += 30, res.getString("apiKeyAlchemyEthereumGoerli"));
        }
    }

    private static JTextField createTextField(RowContentBuilder builder, int top, String caption) {
        builder.addRowLabel(top, caption);
        var txt = new JTextField();
        txt.setPreferredSize(new Dimension(330, 24));
        builder.addRowContent(top, txt);
        return txt;
    }

    public void save(ConfigRepo repo) throws Exception {
        repo.setApiKeyXumm(txtXummApiKey.getText());
        repo.setApiKeyAlchemyEthereumMainnet(txtApiKeyAlchemyEthereumMainnet.getText());
        repo.setApiKeyAlchemyEthereumGoerli(txtApiKeyAlchemyEthereumGoerli.getText());
    }

    public void load(ConfigRepo repo) throws Exception {
        txtXummApiKey.setText(repo.getApiKeyXumm().orElse(null));
        txtApiKeyAlchemyEthereumMainnet.setText(repo.getApiKeyAlchemyEthereumMainnet().orElse(null));
        txtApiKeyAlchemyEthereumGoerli.setText(repo.getApiKeyAlchemyEthereumGoerli().orElse(null));
    }
}
