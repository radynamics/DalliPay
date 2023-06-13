package com.radynamics.dallipay.ui;

import com.alexandriasoftware.swing.action.SplitButtonClickedActionListener;
import com.radynamics.dallipay.cryptoledger.EndpointInfo;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class NetworkPopMenu {
    private final Ledger ledger;
    private final JPopupMenu popupMenu = new JPopupMenu();
    private final ArrayList<Pair<JCheckBoxMenuItem, NetworkInfo>> selectableEntries = new ArrayList<>();

    private final ArrayList<ChangedListener> changedListener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public NetworkPopMenu(Ledger ledger, NetworkInfo[] networks) {
        this.ledger = ledger;

        var index = 0;
        for (var network : networks) {
            addEntry(network, String.format(res.getString("network"), network.getShortText()), loadAsync(network), index++);
        }

        {
            var pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
            popupMenu.add(pnl);
            pnl.setBorder(new EmptyBorder(10, 20, 0, 0));
            pnl.setBackground(popupMenu.getBackground());
            var txt = new JSidechainTextField();
            pnl.add(txt);
            txt.setPreferredSize(new Dimension(180, 21));
            txt.addChangedListener(value -> {
                popupMenu.setVisible(false);
                if (StringUtils.isEmpty(value)) {
                    return;
                }

                HttpUrl httpUrl;
                try {
                    httpUrl = HttpUrl.get(value);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(popupMenu, String.format(res.getString("urlParseFailed"), value), res.getString("connectionFailed"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                var networkInfo = NetworkInfo.create(httpUrl);
                var info = ledger.getEndpointInfo(networkInfo);
                if (info == null) {
                    JOptionPane.showMessageDialog(popupMenu, String.format(res.getString("retrieveServerInfoFailed"), httpUrl), res.getString("connectionFailed"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                var item = addEntryAtEnd(networkInfo, value, CompletableFuture.completedFuture(info));
                onNetworkChanged(item);
            });
        }
    }

    private CompletableFuture<EndpointInfo> loadAsync(NetworkInfo networkInfo) {
        var future = new CompletableFuture<EndpointInfo>();
        Executors.newCachedThreadPool().submit(() -> {
            future.complete(ledger.getEndpointInfo(networkInfo));
        });
        return future;
    }

    private JCheckBoxMenuItem addEntryAtEnd(NetworkInfo networkInfo, String text, CompletableFuture<EndpointInfo> futureInfo) {
        return addEntry(networkInfo, text, futureInfo, popupMenu.getComponentCount() - 1);
    }

    private JCheckBoxMenuItem addEntry(NetworkInfo networkInfo, String text, CompletableFuture<EndpointInfo> futureInfo, int index) {
        var item = new JCheckBoxMenuItem(text);
        item.setToolTipText(res.getString("loading"));

        futureInfo.thenAccept(endpointInfo -> {
            var sb = new StringBuilder();
            sb.append(String.format("%s: %s", res.getString("url"), networkInfo.getUrl()) + System.lineSeparator());
            if (endpointInfo == null) {
                sb.append(res.getString("noInfo"));
            } else {
                if (endpointInfo.getHostId() != null) {
                    sb.append(String.format("%s: %s", res.getString("hostId"), endpointInfo.getHostId()) + System.lineSeparator());
                }
                sb.append(String.format("%s: %s", res.getString("serverVersion"), endpointInfo.getServerVersion()));
            }
            item.setToolTipText(sb.toString());
        });

        popupMenu.add(item, index);
        selectableEntries.add(new ImmutablePair<>(item, networkInfo));
        item.addActionListener((SplitButtonClickedActionListener) e -> onNetworkChanged(item));

        return item;
    }

    private void onNetworkChanged(JCheckBoxMenuItem item) {
        stateChanged(item);
        raiseChanged();
    }

    private void stateChanged(JCheckBoxMenuItem selected) {
        for (var item : selectableEntries) {
            item.getKey().setSelected(item.getKey() == selected);
        }
    }

    public JPopupMenu get() {
        return popupMenu;
    }

    public NetworkInfo getSelectedNetwork() {
        for (var item : selectableEntries) {
            if (item.getKey().isSelected()) {
                return item.getValue();
            }
        }
        return null;
    }

    public void setSelectedNetwork(NetworkInfo network) {
        for (var item : selectableEntries) {
            if (item.getValue().getUrl().equals(network.getUrl())) {
                item.getKey().setSelected(true);
                return;
            }
        }

        var text = network.getId() == null ? network.getUrl().toString() : String.format(res.getString("network"), network.getShortText());
        var item = addEntryAtEnd(network, text, loadAsync(network));
        onNetworkChanged(item);
    }

    public void addChangedListener(ChangedListener l) {
        changedListener.add(l);
    }

    private void raiseChanged() {
        for (var l : changedListener) {
            l.onChanged();
        }
    }
}
