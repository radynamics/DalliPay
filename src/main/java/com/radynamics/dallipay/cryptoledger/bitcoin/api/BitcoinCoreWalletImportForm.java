package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.formdev.flatlaf.FlatClientProperties;
import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.Device;
import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.Hwi;
import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.HwiException;
import com.radynamics.dallipay.ui.ExceptionDialog;
import com.radynamics.dallipay.ui.FormAcceptCloseHandler;
import com.radynamics.dallipay.ui.FormActionListener;
import com.radynamics.dallipay.ui.Utils;
import com.radynamics.dallipay.util.RequestFocusListener;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BitcoinCoreWalletImportForm extends JDialog {
    private final JRadioButton rdoAddress = new JRadioButton();
    private final JRadioButton rdoHardwareWallet = new JRadioButton();
    private final JTextField txtWalletAddress = new JTextField();
    private final ScheduledExecutorService deviceSearchExecutor = Executors.newSingleThreadScheduledExecutor();
    private final JTextField txtWalletName = new JTextField();
    private final JButton cmdOk = new JButton("OK");
    private boolean accepted;
    private final JComboBox<Device> cboDevices = new JComboBox<>();
    private final JLabel lblSearching = new JLabel();
    private final FormAcceptCloseHandler formAcceptCloseHandler = new FormAcceptCloseHandler(this);

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + BitcoinCoreWalletImport.class.getSimpleName());

    public BitcoinCoreWalletImportForm() {
        setTitle(res.getString("title"));
        setIconImage(Utils.getProductIcon());

        formAcceptCloseHandler.configure();
        formAcceptCloseHandler.addFormActionListener(new FormActionListener() {
            @Override
            public void onAccept() {
                deviceSearchExecutor.shutdown();
                acceptDialog();
            }

            @Override
            public void onCancel() {
                deviceSearchExecutor.shutdown();
            }
        });

        var pnlMain = new JPanel();
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(pnlMain);

        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));

        var innerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        JPanel panel0 = new JPanel();
        panel0.setBorder(innerBorder);
        panel0.setLayout(new BoxLayout(panel0, BoxLayout.X_AXIS));
        var panel1 = new JPanel();
        panel1.setBorder(innerBorder);
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        SpringLayout contentLayout = new SpringLayout();
        JPanel pnlContent = new JPanel();
        pnlContent.setLayout(contentLayout);
        JPanel panel3 = new JPanel();
        var panel3Layout = new SpringLayout();
        panel3.setLayout(panel3Layout);

        pnlContent.setPreferredSize(new Dimension(100, 100));
        var sp = new JScrollPane(pnlContent);
        sp.setBorder(BorderFactory.createEmptyBorder());
        panel1.add(sp);

        pnlMain.add(panel0);
        pnlMain.add(panel1);
        pnlMain.add(panel3);

        panel0.setMinimumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel0.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel0.setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));
        panel3.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));

        {
            var pnl = new JPanel();
            pnl.setLayout(new BorderLayout());
            panel0.add(pnl);
            {
                var lbl = new JLabel();
                lbl.setText(getTitle());
                lbl.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
                lbl.setOpaque(true);
                pnl.add(lbl, BorderLayout.NORTH);
            }
            {
                var group = new ButtonGroup();
                rdoAddress.setText(res.getString("setupCustomWallet"));
                group.add(rdoAddress);
                rdoAddress.addItemListener(this::onSelectedWalletTypeChanged);
                rdoAddress.setSelected(true);
                contentLayout.putConstraint(SpringLayout.WEST, rdoAddress, 0, SpringLayout.WEST, pnlContent);
                contentLayout.putConstraint(SpringLayout.NORTH, rdoAddress, 0, SpringLayout.NORTH, pnlContent);
                pnlContent.add(rdoAddress);
                final int LBL_LEFT = 25;
                final int INPUT_LEFT = 90;
                final int OFFSET_TOP = 5;
                {
                    var lbl = new JLabel(res.getString("address"));
                    contentLayout.putConstraint(SpringLayout.WEST, lbl, LBL_LEFT, SpringLayout.WEST, rdoAddress);
                    contentLayout.putConstraint(SpringLayout.NORTH, lbl, OFFSET_TOP, SpringLayout.SOUTH, rdoAddress);
                    pnlContent.add(lbl);

                    txtWalletAddress.setColumns(30);
                    txtWalletAddress.addAncestorListener(new RequestFocusListener());
                    txtWalletAddress.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            onWalletAddressChanged();
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            onWalletAddressChanged();
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            onWalletAddressChanged();
                        }
                    });
                    contentLayout.putConstraint(SpringLayout.WEST, txtWalletAddress, INPUT_LEFT, SpringLayout.WEST, rdoAddress);
                    contentLayout.putConstraint(SpringLayout.NORTH, txtWalletAddress, OFFSET_TOP, SpringLayout.SOUTH, rdoAddress);
                    pnlContent.add(txtWalletAddress);
                }
                rdoHardwareWallet.setText(res.getString("setupHardwareWallet"));
                group.add(rdoHardwareWallet);
                rdoHardwareWallet.addItemListener(this::onSelectedWalletTypeChanged);
                contentLayout.putConstraint(SpringLayout.WEST, rdoHardwareWallet, 0, SpringLayout.WEST, rdoAddress);
                contentLayout.putConstraint(SpringLayout.NORTH, rdoHardwareWallet, 40, SpringLayout.SOUTH, rdoAddress);
                pnlContent.add(rdoHardwareWallet);
                {
                    var lbl = new JLabel(res.getString("device"));
                    contentLayout.putConstraint(SpringLayout.WEST, lbl, LBL_LEFT, SpringLayout.WEST, rdoHardwareWallet);
                    contentLayout.putConstraint(SpringLayout.NORTH, lbl, OFFSET_TOP, SpringLayout.SOUTH, rdoHardwareWallet);
                    pnlContent.add(lbl);
                    cboDevices.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                            return super.getListCellRendererComponent(list, value == null ? "" : ((Device) value).getDisplayText(), index, isSelected, cellHasFocus);
                        }
                    });
                    cboDevices.addItemListener(this::onSelectedDeviceChanged);
                    cboDevices.setPrototypeDisplayValue(new Device("testDevice", "testPath", "testModelName", "123456789"));
                    contentLayout.putConstraint(SpringLayout.WEST, cboDevices, INPUT_LEFT, SpringLayout.WEST, rdoHardwareWallet);
                    contentLayout.putConstraint(SpringLayout.NORTH, cboDevices, OFFSET_TOP, SpringLayout.SOUTH, rdoHardwareWallet);
                    pnlContent.add(cboDevices);

                    lblSearching.setText(res.getString("searching"));
                    lblSearching.setVisible(false);
                    contentLayout.putConstraint(SpringLayout.WEST, lblSearching, 5, SpringLayout.EAST, cboDevices);
                    contentLayout.putConstraint(SpringLayout.NORTH, lblSearching, OFFSET_TOP, SpringLayout.SOUTH, rdoHardwareWallet);
                    pnlContent.add(lblSearching);
                }
            }
            {
                final int OFFSET_TOP = 50;
                var lbl = new JLabel(res.getString("name"));
                contentLayout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnlContent);
                contentLayout.putConstraint(SpringLayout.NORTH, lbl, OFFSET_TOP, SpringLayout.SOUTH, rdoHardwareWallet);
                pnlContent.add(lbl);

                txtWalletName.setColumns(30);
                txtWalletName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "accounting");
                contentLayout.putConstraint(SpringLayout.WEST, txtWalletName, 90, SpringLayout.WEST, rdoAddress);
                contentLayout.putConstraint(SpringLayout.NORTH, txtWalletName, OFFSET_TOP, SpringLayout.SOUTH, rdoHardwareWallet);
                pnlContent.add(txtWalletName);
            }
        }

        {
            var pnl = new JPanel();
            panel3Layout.putConstraint(SpringLayout.EAST, pnl, 0, SpringLayout.EAST, panel3);
            panel3Layout.putConstraint(SpringLayout.SOUTH, pnl, 0, SpringLayout.SOUTH, panel3);
            panel3.add(pnl);
            {
                cmdOk.setPreferredSize(new Dimension(150, 35));
                cmdOk.addActionListener(e -> formAcceptCloseHandler.accept());
                pnl.add(cmdOk);
            }
            {
                var cmd = new JButton(res.getString("cancel"));
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> formAcceptCloseHandler.close());
                pnl.add(cmd);
            }
        }

        refreshOkEnabled();
        startDeviceSearch();
    }

    private void onSelectedWalletTypeChanged(ItemEvent e) {
        txtWalletAddress.setEnabled(rdoAddress.equals(e.getItem()));
        var hardwareWalletSelected = rdoHardwareWallet.equals(e.getItem());
        cboDevices.setEnabled(hardwareWalletSelected);

        if (hardwareWalletSelected && walletName().isEmpty()) {
            onSelectedDeviceChanged(e);
        }

        refreshOkEnabled();
    }

    private void onWalletAddressChanged() {
        if (!rdoAddress.isSelected()) {
            return;
        }
        txtWalletName.setText(walletAddress());
        refreshOkEnabled();
    }

    private void onSelectedDeviceChanged(ItemEvent itemEvent) {
        if (!rdoHardwareWallet.isSelected()) {
            return;
        }
        if (cboDevices.getSelectedItem() != null) {
            var device = (Device) cboDevices.getSelectedItem();
            txtWalletName.setText(device.type());
        }
        refreshOkEnabled();
    }

    private void startDeviceSearch() {
        var hwi = Hwi.get();
        var mutex = new Semaphore(1);
        final var self = this;
        var task = new TimerTask() {
            public synchronized void run() {
                if (!mutex.tryAcquire()) return;

                try {
                    lblSearching.setVisible(true);
                    var devices = hwi.enumerate();
                    cboDevices.removeAllItems();
                    for (var d : devices) {
                        cboDevices.addItem(d);
                    }
                } catch (HwiException e) {
                    ExceptionDialog.show(self, e);
                } finally {
                    lblSearching.setVisible(false);
                    mutex.release();
                }
            }
        };
        deviceSearchExecutor.scheduleAtFixedRate(task, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void refreshOkEnabled() {
        cmdOk.setEnabled(continueEnabled());
    }

    private boolean continueEnabled() {
        if (importWalletAddress() && StringUtils.isEmpty(walletAddress())) return false;
        if (importDevice() && device() == null) return false;
        return walletName().isPresent();
    }

    private void acceptDialog() {
        setDialogAccepted(true);
    }

    private void setDialogAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isDialogAccepted() {
        return accepted;
    }

    public boolean importWalletAddress() {
        return rdoAddress.isSelected();
    }

    public boolean importDevice() {
        return rdoHardwareWallet.isSelected();
    }

    public String walletAddress() {
        return txtWalletAddress.getText().trim();
    }

    public Device device() {
        return (Device) cboDevices.getSelectedItem();
    }

    public LocalDateTime historicTransactionSince() {
        return LocalDateTime.now().minusMonths(3);
    }

    public Optional<String> walletName() {
        var value = txtWalletName.getText().trim();
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }
}
