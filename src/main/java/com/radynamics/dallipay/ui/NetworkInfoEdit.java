package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkId;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.util.RequestFocusListener;
import okhttp3.HttpUrl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ResourceBundle;

public class NetworkInfoEdit {
    private static final ResourceBundle res = ResourceBundle.getBundle("i18n." + NetworkInfoEdit.class.getSimpleName());
    private static final NetworkId custom = new NetworkId("custom", res.getString("custom"));

    public static NetworkInfo show(Component parent, Ledger ledger, HttpUrl url, String displayName) {
        var txtName = new JTextField();
        var txtRpcUrl = new JTextField();
        var txtNetworkId = new JTextField();
        var cboNetwork = createNetworkCombo(ledger, txtNetworkId);

        txtName.setText(displayName);
        txtName.addAncestorListener(new RequestFocusListener());
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("displayName.placeholderText"));
        txtRpcUrl.setText(url == null ? "" : url.toString());
        txtRpcUrl.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("rpcUrl.placeholderText"));
        txtNetworkId.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("networkId.placeholderText"));

        var pnl = new JPanel();
        pnl.setLayout(new GridBagLayout());
        var row = 0;
        appendRow(pnl, res.getString("displayName"), txtName, row++);
        appendRow(pnl, res.getString("rpcUrl"), txtRpcUrl, row++);
        appendRow(pnl, res.getString("network"), cboNetwork, row++);
        appendRow(pnl, res.getString("networkId"), txtNetworkId, row++);
        pnl.setPreferredSize(new Dimension(350, 23 * row));

        int result = JOptionPane.showConfirmDialog(null, pnl, res.getString("descText"), JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        var displayText = txtName.getText().trim();
        var rpcUrlText = txtRpcUrl.getText().trim();
        var selectedNetwork = (NetworkId) cboNetwork.getSelectedItem();
        var networkIdText = custom.getKey().equals(selectedNetwork.getKey()) ? txtNetworkId.getText().trim() : selectedNetwork.getKey();
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

        var info = NetworkInfo.create(httpUrl, displayText);
        info.setNetworkId(toIntegerOrNull(networkIdText));
        return info;
    }

    private static JComboBox<NetworkId> createNetworkCombo(Ledger ledger, JTextField txtNetworkId) {
        var cbo = new JComboBox<NetworkId>();
        cbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, ((NetworkId) value).getDisplayText(), index, isSelected, cellHasFocus);
            }
        });
        cbo.addItemListener(e -> {
            var selected = (NetworkId) e.getItem();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                txtNetworkId.setEnabled(custom.getKey().equals(selected.getKey()));
            }
        });
        for (var item : ledger.networkIds()) {
            cbo.addItem(item);
        }
        cbo.addItem(custom);
        cbo.setSelectedIndex(0);
        return cbo;
    }

    private static void appendRow(JPanel pnl, String labelText, JComponent input, int row) {
        pnl.add(new JLabel(labelText), createGridConstraints(0.3, 1, 0, row));
        pnl.add(input, createGridConstraints(0.7, 1, 1, row));
    }

    private static Integer toIntegerOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
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
