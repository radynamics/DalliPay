package com.radynamics.dallipay.ui;

import com.alexandriasoftware.swing.action.SplitButtonClickedActionListener;
import com.radynamics.dallipay.cryptoledger.EndpointInfo;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.db.ConfigRepo;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private final ArrayList<Pair<JCheckBoxMenuItem, NetworkInfo>> selectableEntries = new ArrayList<>();

    private final ArrayList<ChangedListener> changedListener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public NetworkPopMenu(Ledger ledger, NetworkInfo[] networks) {
        this.ledger = ledger;

        var index = 0;
        for (var network : networks) {
            addEntry(network, network.getShortText(), loadAsync(network), index++);
        }

        {
            var pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
            popupMenu.add(pnl);
            pnl.setBorder(new EmptyBorder(10, 20, 0, 0));
            pnl.setBackground(popupMenu.getBackground());
            var txt = new JSidechainTextField();
            pnl.add(txt);
            txt.setPreferredSize(new Dimension(180, 21));
            txt.addChangedListener(new SidechainChangedListener() {
                @Override
                public void onChanged(NetworkInfo networkInfo) {
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

                    var item = addEntryAtEnd(networkInfo, networkInfo.getShortText(), CompletableFuture.completedFuture(info));
                    onNetworkChanged(item);
                }

                @Override
                public void onCreated(NetworkInfo networkInfo) {
                    saveCustoms(networkInfo);
                }
            });
        }
    }

    private void saveCustoms(NetworkInfo networkInfo) {
        var entries = getCustomEntries();
        entries.add(networkInfo);

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
            if (Arrays.stream(ledger.getDefaultNetworkInfo()).noneMatch(o -> o.sameNet(e.getValue()))) {
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
        item.setToolTipText(res.getString("loading"));

        futureInfo.thenAccept(endpointInfo -> item.setToolTipText(createToolTipText(networkInfo, endpointInfo, null)))
                .exceptionally((e) -> {
                    log.error(e.getMessage(), e);
                    item.setToolTipText(createToolTipText(networkInfo, null, e));
                    return null;
                });

        popupMenu.add(item, index);
        selectableEntries.add(new ImmutablePair<>(item, networkInfo));
        item.addActionListener((SplitButtonClickedActionListener) e -> onNetworkChanged(item));

        return item;
    }

    private String createToolTipText(NetworkInfo networkInfo, EndpointInfo endpointInfo, Throwable e) {
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
        if (e != null) {
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append(String.format("Error: %s", e.getCause().getMessage()));
        }
        return sb.toString();
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

        var text = network.getId() == null ? network.getUrl().toString() : network.getShortText();
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
