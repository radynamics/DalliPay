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

    private final int LINE_HEIGHT = 25;
    private final int INPUT_LEFT = 120;

    private static final ResourceBundle res = ResourceBundle.getBundle("i18n." + NetworkInfoEdit.class.getSimpleName());
    private static final ResourceBundle resNetworkPopMenu = ResourceBundle.getBundle("i18n." + NetworkPopMenu.class.getSimpleName());
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

        var l = new SpringLayout();
        setLayout(l);

        var row = 0;
        appendRow(l, res.getString("displayName"), txtName, row++);
        appendRow(l, res.getString("rpcUrl"), txtRpcUrl, row++);
        appendRow(l, res.getString("network"), cboNetwork, row++);
        appendRow(l, res.getString("networkId"), txtNetworkId, row++);
        {
            var pnlTestConnection = new JPanel();
            add(pnlTestConnection);
            pnlTestConnection.setLayout(new BoxLayout(pnlTestConnection, BoxLayout.Y_AXIS));
            l.putConstraint(SpringLayout.WEST, pnlTestConnection, INPUT_LEFT, SpringLayout.WEST, this);
            l.putConstraint(SpringLayout.NORTH, pnlTestConnection, row * LINE_HEIGHT + 5, SpringLayout.NORTH, this);
            l.putConstraint(SpringLayout.EAST, pnlTestConnection, 0, SpringLayout.EAST, this);
            l.putConstraint(SpringLayout.SOUTH, pnlTestConnection, 0, SpringLayout.SOUTH, this);

            var txt = new JTextArea();

            var cmd = new JButton(res.getString("testConnection"));
            pnlTestConnection.add(cmd);
            cmd.addActionListener(e -> {
                try {
                    var ni = createNetworkInfo(true);
                    if (ni == null) {
                        return;
                    }

                    var info = ledger.getEndpointInfo(ni);
                    if (info == null) {
                        txt.setText(resNetworkPopMenu.getString("retrieveServerInfoFailed"));
                    } else {
                        var sb = new StringBuilder();
                        sb.append(res.getString("testSuccess").formatted(displayName));
                        sb.append(System.lineSeparator());
                        sb.append(info.getServerVersion());
                        txt.setText(sb.toString());
                    }
                } catch (Exception ex) {
                    txt.setText(ex.getMessage());
                }
            });

            pnlTestConnection.add(Box.createRigidArea(new Dimension(0, 5)));

            var sp = new JScrollPane(txt);
            pnlTestConnection.add(sp);
            sp.setAlignmentX(Component.LEFT_ALIGNMENT);
            txt.setEditable(false);
            txt.setLineWrap(true);
        }
    }

    public NetworkInfo createNetworkInfo(boolean showErrors) {
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
            if (showErrors) {
                JOptionPane.showMessageDialog(this, String.format(res.getString("urlParseFailed"), rpcUrlText), res.getString("connectionFailed"), JOptionPane.INFORMATION_MESSAGE);
            }
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

    private void appendRow(SpringLayout l, String labelText, JComponent input, int row) {
        var lbl = new JLabel(labelText);
        add(lbl);
        l.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, this);
        l.putConstraint(SpringLayout.NORTH, lbl, row * LINE_HEIGHT, SpringLayout.NORTH, this);

        add(input);
        l.putConstraint(SpringLayout.WEST, input, INPUT_LEFT, SpringLayout.WEST, this);
        l.putConstraint(SpringLayout.NORTH, input, row * LINE_HEIGHT, SpringLayout.NORTH, this);
        l.putConstraint(SpringLayout.EAST, input, 0, SpringLayout.EAST, this);
    }

    private static Integer toIntegerOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static NetworkInfo show(Frame owner, Component parent, Ledger ledger, HttpUrl url, String displayName) {
        var networkInfoEdit = new NetworkInfoEdit(ledger, url, displayName);
        var frm = new GeneralDialog(owner, res.getString("descText"), networkInfoEdit);
        frm.smallOkCancel();
        frm.setSize(450, 350);
        frm.setLocationRelativeTo(parent);
        frm.setResizable(false);
        frm.setVisible(true);
        if (!frm.accepted()) {
            return null;
        }
        return networkInfoEdit.createNetworkInfo(false);
    }

    @Override
    public JComponent view() {
        return this;
    }

    @Override
    public boolean validateInput() {
        return createNetworkInfo(true) != null;
    }
}
