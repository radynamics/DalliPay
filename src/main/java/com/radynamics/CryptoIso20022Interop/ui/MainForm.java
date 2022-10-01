package com.radynamics.CryptoIso20022Interop.ui;

import com.alexandriasoftware.swing.JSplitButton;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.VersionController;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.Pain001Reader;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.update.OnlineUpdate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_MINIMUM_TAB_WIDTH;

public class MainForm extends JFrame {
    private final TransformInstruction transformInstruction;
    private SendForm sendingPanel;
    private ReceiveForm receivingPanel;
    private OptionsForm optionsPanel;
    private JSplitButton cmdNetwork;

    public MainForm(TransformInstruction transformInstruction) {
        if (transformInstruction == null) throw new IllegalArgumentException("Parameter 'transformInstruction' cannot be null");
        this.transformInstruction = transformInstruction;

        setupUI();
    }

    private void setupUI() {
        var vc = new VersionController();
        setTitle(String.format("CryptoIso20022Interop [%s]", vc.getVersion()));
        setIconImage(Utils.getProductIcon());

        setJMenuBar(createMenuBar());

        var pnlMain = new JPanel();
        add(pnlMain);
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel();
        var mainContentBorder = new EmptyBorder(0, 10, 10, 10);
        final int TABBEDPANE_WIDTH = 100;
        {
            final int HEIGHT = 80;
            var pnl = new JPanel();
            pnlMain.add(pnl);
            pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
            pnl.setMinimumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
            pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
            pnl.setPreferredSize(new Dimension(500, HEIGHT));
            {
                var lbl = new JLabel();
                lbl.setIcon(Utils.getScaled("img/productIcon.png", 32, 32));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setMinimumSize(new Dimension(TABBEDPANE_WIDTH, HEIGHT));
                lbl.setMaximumSize(new Dimension(TABBEDPANE_WIDTH, HEIGHT));
                lbl.setPreferredSize(new Dimension(TABBEDPANE_WIDTH, HEIGHT));
                pnl.add(lbl);
            }
            {
                lblTitle.setBorder(BorderFactory.createEmptyBorder(0, mainContentBorder.getBorderInsets().left, 0, 0));
                pnl.add(lblTitle);
                lblTitle.putClientProperty("FlatLaf.styleClass", "h1");
            }
        }
        {
            var pnl = new JPanel();
            pnlMain.add(pnl);
            pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
            {
                var tabbedPane = new JTabbedPane();
                pnl.add(tabbedPane);
                tabbedPane.putClientProperty(TABBED_PANE_MINIMUM_TAB_WIDTH, TABBEDPANE_WIDTH);
                tabbedPane.setTabPlacement(JTabbedPane.LEFT);
                tabbedPane.addChangeListener(e -> {
                    var selected = tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
                    if (selected instanceof MainFormPane) {
                        lblTitle.setText(((MainFormPane) selected).getTitle());
                    } else {
                        lblTitle.setText("");
                    }
                });

                {
                    var provider = transformInstruction.getExchangeRateProvider();
                    provider.load();

                    sendingPanel = new SendForm(this, transformInstruction, new CurrencyConverter(provider.latestRates()));
                    sendingPanel.setBorder(mainContentBorder);
                    sendingPanel.setReader(new Pain001Reader(transformInstruction.getLedger()));
                    tabbedPane.addTab("Send", sendingPanel);
                }
                {
                    receivingPanel = new ReceiveForm(transformInstruction, new CurrencyConverter());
                    receivingPanel.setBorder(mainContentBorder);
                    tabbedPane.addTab("Receive", receivingPanel);
                }
                {
                    tabbedPane.addTab("", new JPanel());
                    tabbedPane.setEnabledAt(2, false);
                }
                {
                    optionsPanel = new OptionsForm(this);
                    optionsPanel.addChangedListener(() -> {
                        transformInstruction.getHistoricExchangeRateSource().init();
                        receivingPanel.refreshTargetCcys();
                    });
                    optionsPanel.setBorder(mainContentBorder);
                    tabbedPane.addTab("Options", optionsPanel);
                    optionsPanel.load();
                }
            }
        }
    }

    private JMenuBar createMenuBar() {
        var menuBar = new JMenuBar();
        menuBar.add(Box.createGlue());

        {
            OnlineUpdate.search().thenAccept((updateInfo) -> {
                if (updateInfo == null) {
                    return;
                }
                var cmdUpdate = new FlatButton();
                var icon = new FlatSVGIcon("svg/update.svg", 16, 16);
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Consts.ColorAccent));
                cmdUpdate.setIcon(icon);
                var text = String.format("Update to newer version %s available", updateInfo.getVersion());
                cmdUpdate.setToolTipText(text);
                cmdUpdate.setButtonType(FlatButton.ButtonType.toolBarButton);
                cmdUpdate.setFocusable(false);
                cmdUpdate.addActionListener(e -> {
                    Utils.openBrowser(this, updateInfo.getUri());
                });
                menuBar.add(cmdUpdate);
                menuBar.updateUI();

                int ret = JOptionPane.showConfirmDialog(this, String.format("%s. Do you want to update now?", text), "Update", JOptionPane.YES_NO_CANCEL_OPTION);
                if (ret == JOptionPane.YES_OPTION) {
                    Utils.openBrowser(this, updateInfo.getUri());
                }
            });
        }

        var livenet = transformInstruction.getConfig().getNetwork(Network.Live);
        var testnet = transformInstruction.getConfig().getNetwork(Network.Test);
        var popupMenu = new NetworkPopMenu(new NetworkInfo[]{livenet, testnet});
        popupMenu.setSelectedNetwork(transformInstruction.getNetwork());
        popupMenu.addChangedListener(() -> {
            var selected = popupMenu.getSelectedNetwork();
            if (selected == null) {
                return;
            }
            transformInstruction.setNetwork(selected);
            refreshNetworkButton();
            sendingPanel.reload();
        });

        final String DROPDOWN_ARROW_OVERLAP_HACK = "     ";
        cmdNetwork = new JSplitButton(DROPDOWN_ARROW_OVERLAP_HACK);
        refreshNetworkButton();
        cmdNetwork.setBorder(BorderFactory.createEmptyBorder());
        cmdNetwork.setAlwaysPopup(true);
        cmdNetwork.setPopupMenu(popupMenu.get());
        menuBar.add(cmdNetwork);

        return menuBar;
    }

    private void refreshNetworkButton() {
        var icon = new FlatSVGIcon("svg/network.svg", 16, 16);
        var networkInfo = transformInstruction.getNetwork();
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> NetworkInfo.liveId.equals(networkInfo.getId()) ? Consts.ColorLivenet : Consts.ColorTestnet));
        cmdNetwork.setIcon(icon);
        cmdNetwork.setToolTipText(String.format("Currently using %s network (%s)", networkInfo.getShortText(), networkInfo.getUrl()));
    }

    public void setInputFileName(String inputFileName) {
        sendingPanel.setInput(inputFileName);
    }

    public void setReceivingWallet(Wallet wallet) {
        receivingPanel.setWallet(wallet);
    }

    public void setOutputFileName(String outputFileName) {
        receivingPanel.setTargetFileName(outputFileName);
    }

    public void setPeriod(DateTimeRange period) {
        receivingPanel.setPeriod(period);
    }
}
