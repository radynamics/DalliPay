package com.radynamics.dallipay.ui;

import com.alexandriasoftware.swing.action.SplitButtonClickedActionListener;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.cryptoledger.EndpointInfo;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.util.RequestFocusListener;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class NetworkPopMenu {
    private final static Logger log = LogManager.getLogger(NetworkPopMenu.class);
    private final Ledger ledger;
    private final JPopupMenu popupMenu = new JPopupMenu();
    private final JSidechainTextField txt;
    private final ArrayList<Pair<JCheckBoxMenuItem, NetworkInfo>> selectableEntries = new ArrayList<>();
    private final JCheckBoxMenuItem noConnectionsEntry;

    private final ArrayList<ChangedListener> changedListener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public NetworkPopMenu(Frame owner, Ledger ledger, NetworkInfo[] networks) {
        this.ledger = ledger;

        noConnectionsEntry = new JCheckBoxMenuItem(res.getString("noConnections"));
        noConnectionsEntry.setEnabled(false);
        popupMenu.add(noConnectionsEntry, 0);

        var index = 1;
        for (var network : networks) {
            addEntry(network, network.getShortText(), loadAsync(network), index++);
        }

        refreshNoConnectionsEntryVisibility();

        {
            var pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
            popupMenu.add(pnl);
            pnl.setBorder(new EmptyBorder(10, 20, 0, 0));
            pnl.setBackground(popupMenu.getBackground());
            txt = new JSidechainTextField(owner, ledger);
            pnl.add(txt);
            popupMenu.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    txt.addAncestorListener(new RequestFocusListener());
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
            txt.setPreferredSize(new Dimension(180, 21));
            txt.addChangedListener(new SidechainChangedListener() {
                @Override
                public void onCreated(NetworkInfo networkInfo) {
                    popupMenu.setVisible(false);

                    EndpointInfo info = null;
                    try {
                        info = ledger.getEndpointInfo(networkInfo);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    if (info == null) {
                        JOptionPane.showMessageDialog(popupMenu, String.format(res.getString("retrieveServerInfoFailed"), networkInfo.getUrl()), res.getString("connectionFailed"), JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    var entries = getCustomEntries();
                    entries.add(networkInfo);
                    saveCustoms(entries);

                    var item = addEntryAtEnd(networkInfo, networkInfo.getShortText(), CompletableFuture.completedFuture(info));
                    onNetworkChanged(item);
                }
            });
        }
    }

    private void refreshNoConnectionsEntryVisibility() {
        noConnectionsEntry.setVisible(selectableEntries.isEmpty());
    }

    private void saveCustoms(ArrayList<NetworkInfo> entries) {
        try (var repo = new ConfigRepo()) {
            repo.setCustomSidechains(ledger, entries.toArray(NetworkInfo[]::new));
            repo.commit();
        } catch (Exception e) {
            ExceptionDialog.show(null, e);
        }
    }

    private ArrayList<NetworkInfo> getCustomEntries() {
        var list = new ArrayList<NetworkInfo>();
        for (var e : selectableEntries) {
            if (!e.getValue().isPredefined()) {
                list.add(e.getValue());
            }
        }
        return list;
    }

    private CompletableFuture<EndpointInfo> loadAsync(NetworkInfo networkInfo) {
        var future = new CompletableFuture<EndpointInfo>();
        Executors.newCachedThreadPool().submit(() -> {
            try {
                future.complete(ledger.getEndpointInfo(networkInfo));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private JCheckBoxMenuItem addEntryAtEnd(NetworkInfo networkInfo, String text, CompletableFuture<EndpointInfo> futureInfo) {
        return addEntry(networkInfo, text, futureInfo, popupMenu.getComponentCount() - 1);
    }

    private JCheckBoxMenuItem addEntry(NetworkInfo networkInfo, String text, CompletableFuture<EndpointInfo> futureInfo, int index) {
        var item = new JCheckBoxMenuItem(text);
        item.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        item.setToolTipText(res.getString("loading"));

        futureInfo.thenAccept(endpointInfo -> item.setToolTipText(createToolTipText(networkInfo, endpointInfo, null)))
                .exceptionally((e) -> {
                    log.error(e.getMessage(), e);
                    item.setEnabled(false);
                    item.setToolTipText(createToolTipText(networkInfo, null, e));
                    return null;
                });

        if (!networkInfo.isPredefined()) {
            var cmd = new JButton();
            item.add(cmd);
            cmd.setIcon(new FlatSVGIcon("svg/delete.svg", 16, 16));
            cmd.setMargin(new Insets(0, 2, 0, 2));
            cmd.addActionListener(e -> onDelete(item, networkInfo));
        }
        popupMenu.add(item, index);
        selectableEntries.add(new ImmutablePair<>(item, networkInfo));
        item.addActionListener((SplitButtonClickedActionListener) e -> onNetworkChanged(item));
        refreshNoConnectionsEntryVisibility();

        return item;
    }

    private void onDelete(JCheckBoxMenuItem item, NetworkInfo networkInfo) {
        var entries = getCustomEntries();
        entries.removeIf(o -> o.sameAs(networkInfo));
        saveCustoms(entries);

        selectableEntries.removeIf(o -> o.getRight().sameAs(networkInfo));
        popupMenu.remove(item);

        if (item.isSelected()) {
            setSelectedNetwork(selectableEntries.isEmpty() ? null : selectableEntries.get(0).getRight());
        }

        refreshNoConnectionsEntryVisibility();

        // Force correct repaint
        popupMenu.setVisible(false);
        popupMenu.setVisible(true);
    }

    private String createToolTipText(NetworkInfo networkInfo, EndpointInfo endpointInfo, Throwable e) {
        var sb = new StringBuilder();
        sb.append(String.format("%s: %s", res.getString("url"), Utils.hideCredentials(networkInfo.getUrl())) + System.lineSeparator());
        sb.append(String.format("%s: %s", res.getString("network"), getNetworkIdText(networkInfo.getNetworkId())) + System.lineSeparator());
        if (endpointInfo == null) {
            sb.append(res.getString("noInfo"));
        } else {
            if (endpointInfo.getHostId() != null) {
                sb.append(String.format("%s: %s", res.getString("hostId"), endpointInfo.getHostId()) + System.lineSeparator());
            }
            sb.append(String.format("%s: %s", res.getString("serverVersion"), endpointInfo.getServerVersion()));
        }
        if (e != null) {
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append(String.format("Error: %s", e.getCause().getMessage()));
        }
        return sb.toString();
    }

    private String getNetworkIdText(Integer networkId) {
        var networkIdText = String.valueOf(networkId);
        var entry = Arrays.stream(ledger.networkIds()).filter(o -> o.getKey().equals(networkIdText)).findFirst();
        return entry.isPresent() ? entry.get().getDisplayText() : res.getString("unknown").formatted(networkIdText);
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

    public void showNetworkInfoEdit(String value) {
        txt.showNetworkInfoEdit(value);
    }

    public boolean hasSelectableNetworks() {
        return !selectableEntries.isEmpty();
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
        if (network == null) {
            onNetworkChanged(null);
            return;
        }
        for (var item : selectableEntries) {
            if (item.getValue().getUrl().equals(network.getUrl())) {
                item.getKey().setSelected(true);
                onNetworkChanged(item.getLeft());
                return;
            }
        }

        var text = network.getId() == null ? Utils.hideCredentials(network.getUrl()).toString() : network.getShortText();
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
