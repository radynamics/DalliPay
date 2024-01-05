package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.Device;
import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.Hwi;
import com.radynamics.dallipay.cryptoledger.signing.SigningException;
import com.radynamics.dallipay.ui.FormAcceptCloseHandler;
import com.radynamics.dallipay.ui.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BitcoinCoreWalletImportForm extends JDialog {
    private final JRadioButton rdoAddress = new JRadioButton();
    private final JRadioButton rdoHardwareWallet = new JRadioButton();
    private final JTextField txtWalletAddress = new JTextField();
    private boolean accepted;
    private final JComboBox<Device> cboDevices = new JComboBox<>();
    private final JLabel lblSearching = new JLabel();
    private final FormAcceptCloseHandler formAcceptCloseHandler = new FormAcceptCloseHandler(this);

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + BitcoinCoreWalletImport.class.getSimpleName());

    public BitcoinCoreWalletImportForm() {
        setTitle(res.getString("title"));
        setIconImage(Utils.getProductIcon());

        formAcceptCloseHandler.configure();
        formAcceptCloseHandler.addFormActionListener(this::acceptDialog);

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
                rdoAddress.addItemListener(this::onSelectedChanged);
                rdoAddress.setSelected(true);
                contentLayout.putConstraint(SpringLayout.WEST, rdoAddress, 0, SpringLayout.WEST, pnlContent);
                contentLayout.putConstraint(SpringLayout.NORTH, rdoAddress, 0, SpringLayout.NORTH, pnlContent);
                pnlContent.add(rdoAddress);
                {
                    var pnlRdo = new JPanel();
                    contentLayout.putConstraint(SpringLayout.WEST, pnlRdo, 20, SpringLayout.WEST, rdoAddress);
                    contentLayout.putConstraint(SpringLayout.NORTH, pnlRdo, 0, SpringLayout.SOUTH, rdoAddress);
                    pnlContent.add(pnlRdo);
                    pnlRdo.add(new JLabel(res.getString("address")));
                    pnlRdo.add(txtWalletAddress);
                }
                rdoHardwareWallet.setText(res.getString("setupHardwareWallet"));
                group.add(rdoHardwareWallet);
                rdoHardwareWallet.addItemListener(this::onSelectedChanged);
                contentLayout.putConstraint(SpringLayout.WEST, rdoHardwareWallet, 0, SpringLayout.WEST, rdoAddress);
                contentLayout.putConstraint(SpringLayout.NORTH, rdoHardwareWallet, 40, SpringLayout.SOUTH, rdoAddress);
                pnlContent.add(rdoHardwareWallet);
                {
                    var pnlRdo = new JPanel();
                    contentLayout.putConstraint(SpringLayout.WEST, pnlRdo, 20, SpringLayout.WEST, rdoHardwareWallet);
                    contentLayout.putConstraint(SpringLayout.NORTH, pnlRdo, 0, SpringLayout.SOUTH, rdoHardwareWallet);
                    pnlContent.add(pnlRdo);
                    pnlRdo.add(new JLabel(res.getString("device")));
                    cboDevices.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                            return super.getListCellRendererComponent(list, value == null ? "" : ((Device) value).getDisplayText(), index, isSelected, cellHasFocus);
                        }
                    });
                    pnlRdo.add(cboDevices);

                    lblSearching.setText(res.getString("searching"));
                    lblSearching.setVisible(false);
                    pnlRdo.add(lblSearching);
                }
            }
        }

        {
            var pnl = new JPanel();
            panel3Layout.putConstraint(SpringLayout.EAST, pnl, 0, SpringLayout.EAST, panel3);
            panel3Layout.putConstraint(SpringLayout.SOUTH, pnl, 0, SpringLayout.SOUTH, panel3);
            panel3.add(pnl);
            {
                var cmd = new JButton("OK");
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> formAcceptCloseHandler.accept());
                pnl.add(cmd);
            }
            {
                var cmd = new JButton(res.getString("cancel"));
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> formAcceptCloseHandler.close());
                pnl.add(cmd);
            }
        }

        startDeviceSearch();
    }

    private void onSelectedChanged(ItemEvent e) {
        txtWalletAddress.setEnabled(rdoAddress.equals(e.getItem()));
        cboDevices.setEnabled(rdoHardwareWallet.equals(e.getItem()));
    }

    private void startDeviceSearch() {
        var hwi = new Hwi();
        var mutex = new Semaphore(1);
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
                } catch (SigningException e) {
                    throw new RuntimeException(e);
                } finally {
                    lblSearching.setVisible(false);
                    mutex.release();
                }
            }
        };
        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(task, 0, 1000, TimeUnit.MILLISECONDS);
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
}
