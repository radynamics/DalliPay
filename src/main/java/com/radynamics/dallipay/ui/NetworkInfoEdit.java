package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.util.RequestFocusListener;
import okhttp3.HttpUrl;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class NetworkInfoEdit {
    private static final ResourceBundle res = ResourceBundle.getBundle("i18n." + NetworkInfoEdit.class.getSimpleName());

    public static NetworkInfo show(Component parent, NetworkInfo ni) {
        var txtName = new JTextField();
        var txtRpcUrl = new JTextField();

        txtName.setText(ni.getDisplayName());
        txtName.addAncestorListener(new RequestFocusListener());
        txtRpcUrl.setText(ni.getUrl().toString());

        var pnl = new JPanel();
        pnl.setLayout(new GridBagLayout());
        pnl.setPreferredSize(new Dimension(350, 50));
        pnl.add(new JLabel(res.getString("displayName")), createGridConstraints(0.3, 1, 0, 0));
        pnl.add(txtName, createGridConstraints(0.7, 1, 1, 0));
        pnl.add(new JLabel(res.getString("rpcUrl")), createGridConstraints(0.3, 1, 0, 1));
        pnl.add(txtRpcUrl, createGridConstraints(0.7, 1, 1, 1));

        int result = JOptionPane.showConfirmDialog(null, pnl, res.getString("descText"), JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        var displayText = txtName.getText().trim();
        var rpcUrlText = txtRpcUrl.getText().trim();
        if (displayText.length() == 0 || rpcUrlText.length() == 0) {
            return null;
        }

        HttpUrl httpUrl;
        try {
            httpUrl = HttpUrl.get(rpcUrlText);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, String.format(res.getString("urlParseFailed"), rpcUrlText), res.getString("connectionFailed"), JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        return NetworkInfo.create(httpUrl, displayText);
    }

    private static GridBagConstraints createGridConstraints(double weightx, double weighty, int x, int y) {
        var c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = weightx;
        c.weighty = weighty;
        c.gridx = x;
        c.gridy = y;
        return c;
    }
}
