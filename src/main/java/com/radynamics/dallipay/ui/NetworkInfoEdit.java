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

public class NetworkInfoEdit extends JPanel implements GeneralDialogContent {
    private final JTextField txtName;
    private final JTextField txtRpcUrl;
    private final JTextField txtNetworkId;
    private final JComboBox<NetworkId> cboNetwork;

    private static final ResourceBundle res = ResourceBundle.getBundle("i18n." + NetworkInfoEdit.class.getSimpleName());
    private static final NetworkId custom = new NetworkId("custom", res.getString("custom"));

    public NetworkInfoEdit(Ledger ledger, HttpUrl url, String displayName) {
        txtName = new JTextField();
        txtRpcUrl = new JTextField();
        txtNetworkId = new JTextField();
        cboNetwork = createNetworkCombo(ledger, txtNetworkId);

        txtName.setText(displayName);
        txtName.addAncestorListener(new RequestFocusListener());
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("displayName.placeholderText"));
        txtRpcUrl.setText(url == null ? "" : url.toString());
        txtRpcUrl.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("rpcUrl.placeholderText"));
        txtNetworkId.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("networkId.placeholderText"));

        setLayout(new GridBagLayout());
        var row = 0;
        appendRow(res.getString("displayName"), txtName, row++);
        appendRow(res.getString("rpcUrl"), txtRpcUrl, row++);
        appendRow(res.getString("network"), cboNetwork, row++);
        appendRow(res.getString("networkId"), txtNetworkId, row++);
        setPreferredSize(new Dimension(350, 23 * row));
    }

    public NetworkInfo networkInfo() {
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
            JOptionPane.showMessageDialog(this, String.format(res.getString("urlParseFailed"), rpcUrlText), res.getString("connectionFailed"), JOptionPane.INFORMATION_MESSAGE);
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

    private void appendRow(String labelText, JComponent input, int row) {
        add(new JLabel(labelText), createGridConstraints(0.3, 1, 0, row));
        add(input, createGridConstraints(0.7, 1, 1, row));
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

    public static NetworkInfo show(Frame owner, Component parent, Ledger ledger, HttpUrl url, String displayName) {
        var networkInfoEdit = new NetworkInfoEdit(ledger, url, displayName);
        var frm = new GeneralDialog(owner, res.getString("descText"), networkInfoEdit);
        frm.smallOkCancel();
        frm.setSize(400, 200);
        frm.setLocationRelativeTo(parent);
        frm.setResizable(false);
        frm.setVisible(true);
        if (!frm.accepted()) {
            return null;
        }
        return networkInfoEdit.networkInfo();
    }

    @Override
    public JComponent view() {
        return this;
    }
}
