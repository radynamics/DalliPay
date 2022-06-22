package com.radynamics.CryptoIso20022Interop.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.Address;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SendConfirmationForm extends JDialog {
    private final Payment[] payments;
    private final ExchangeRateProvider provider;
    private int totalPaymentCount;

    private SpringLayout panel1Layout;
    private JPanel pnlContent;
    private Component anchorComponentTopLeft;
    private boolean accepted;
    private JButton cmdSend;
    private JLabel lblFee;

    public SendConfirmationForm(Payment[] payments, ExchangeRateProvider provider, int totalPaymentCount) {
        this.payments = payments;
        this.provider = provider;
        this.totalPaymentCount = totalPaymentCount;
        setupUI();
    }

    private void setupUI() {
        setTitle("Confirm Payments");
        setIconImage(Utils.getProductIcon());

        var cancelDialog = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        };
        getRootPane().registerKeyboardAction(cancelDialog, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        var acceptDialog = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOk();
            }
        };
        getRootPane().registerKeyboardAction(acceptDialog, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

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
        panel1Layout = new SpringLayout();
        pnlContent = new JPanel();
        pnlContent.setLayout(panel1Layout);
        var panel2 = new JPanel();
        panel2.setBorder(innerBorder);
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
        var panel2Layout = new SpringLayout();
        var pnlFeeContent = new JPanel();
        pnlFeeContent.setLayout(panel2Layout);
        panel2.add(pnlFeeContent);
        JPanel panel3 = new JPanel();
        var panel3Layout = new SpringLayout();
        panel3.setLayout(panel3Layout);

        pnlContent.setPreferredSize(new Dimension(100, 120));
        var sp = new JScrollPane(pnlContent);
        sp.setBorder(BorderFactory.createEmptyBorder());
        panel1.add(sp);

        pnlMain.add(panel0);
        pnlMain.add(panel1);
        pnlMain.add(panel2);
        pnlMain.add(panel3);

        panel0.setMinimumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel0.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel0.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));
        panel2.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel2.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
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
                pnl.add(lbl, BorderLayout.PAGE_START);
            }
            {
                var lbl = new JLabel();
                lbl.setText("Please review following summary before sending. Sent payments cannot be reversed.");
                lbl.setOpaque(true);
                pnl.add(lbl, BorderLayout.WEST);
            }
            {
                if (payments.length != totalPaymentCount) {
                    var pnlInfo = new JPanel();
                    pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.X_AXIS));
                    pnl.add(pnlInfo, BorderLayout.PAGE_END);
                    {
                        var lbl = new JLabel();
                        lbl.setIcon(new FlatSVGIcon("svg/informationDialog.svg", 16, 16));
                        pnlInfo.add(lbl);
                    }
                    pnlInfo.add(Box.createRigidArea(new Dimension(5, 0)));
                    {
                        var lbl = new JLabel(String.format("Selected %s payments of %s available.", payments.length, totalPaymentCount));
                        lbl.setOpaque(true);
                        pnlInfo.add(lbl);
                    }
                }
            }
        }

        {
            int row = 0;
            var sendingWallets = PaymentUtils.distinctSendingWallets(payments);
            for (var w : sendingWallets) {
                var payments = PaymentUtils.fromSender(w, this.payments);
                if (payments.size() == 0) {
                    continue;
                }
                var created = createRow(row++, w, getSenderAccount(payments), getSenderAddress(payments), payments);
                anchorComponentTopLeft = anchorComponentTopLeft == null ? created : anchorComponentTopLeft;
            }
        }
        {
            var lblFeeText = new JLabel("Total expected Transaction fee");
            panel2Layout.putConstraint(SpringLayout.WEST, lblFeeText, 0, SpringLayout.WEST, pnlFeeContent);
            panel2Layout.putConstraint(SpringLayout.NORTH, lblFeeText, 0, SpringLayout.NORTH, pnlFeeContent);
            pnlFeeContent.add(lblFeeText);

            lblFee = new JLabel();
            refreshTotalFee();
            panel2Layout.putConstraint(SpringLayout.WEST, lblFee, 10, SpringLayout.EAST, lblFeeText);
            panel2Layout.putConstraint(SpringLayout.NORTH, lblFee, 0, SpringLayout.NORTH, pnlFeeContent);
            pnlFeeContent.add(lblFee);

            var lbl3 = Utils.createLinkLabel(this, "edit...");
            panel2Layout.putConstraint(SpringLayout.WEST, lbl3, 10, SpringLayout.EAST, lblFee);
            panel2Layout.putConstraint(SpringLayout.NORTH, lbl3, 0, SpringLayout.NORTH, pnlFeeContent);
            lbl3.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        showFeeEdit();
                    }
                }
            });
            pnlFeeContent.add(lbl3);
        }
        {
            var pnl = new JPanel();
            panel3Layout.putConstraint(SpringLayout.EAST, pnl, 0, SpringLayout.EAST, panel3);
            panel3Layout.putConstraint(SpringLayout.SOUTH, pnl, 0, SpringLayout.SOUTH, panel3);
            panel3.add(pnl);
            {
                cmdSend = new JButton("Send");
                cmdSend.setPreferredSize(new Dimension(150, 35));
                cmdSend.addActionListener(e -> onOk());
                pnl.add(cmdSend);
            }
            {
                var cmd = new JButton("Cancel");
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> close());
                pnl.add(cmd);
            }
        }

        startTimeoutCountdown();
    }

    private void refreshTotalFee() {
        lblFee.setText(PaymentUtils.totalFeesText(payments, provider));
    }

    private Component createRow(int row, Wallet sendingWallet, Account senderAccount, Address senderAddress, ArrayList<Payment> payments) {
        Ledger l = payments.get(0).getLedger();

        var padNorth = row == 0 ? 20 : getNorthPad(row);
        var lbl = new WalletLabel();
        lbl.setWallet(sendingWallet)
                .setLedger(l)
                .setAccount(senderAccount)
                .setAddress(senderAddress)
                .setWalletInfoAggregator(new WalletInfoAggregator(l.getInfoProvider()))
                .setInfoLineCount(2);
        panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnlContent);
        panel1Layout.putConstraint(SpringLayout.NORTH, lbl, padNorth, SpringLayout.NORTH, pnlContent);
        pnlContent.add(lbl);

        var sumsFiatText = PaymentUtils.sumString(payments);
        var sums = PaymentUtils.sumLedgerUnit(payments);
        var sumLedgerText = MoneyFormatter.formatLedger(sums);
        var text = String.format("%s (%s, %s payments)", sumsFiatText, sumLedgerText, payments.size());

        var lblPayments = new JLabel(text);
        panel1Layout.putConstraint(SpringLayout.WEST, lblPayments, 30, SpringLayout.EAST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
        panel1Layout.putConstraint(SpringLayout.NORTH, lblPayments, padNorth, SpringLayout.NORTH, pnlContent);
        pnlContent.add(lblPayments);

        return lbl;
    }

    private Account getSenderAccount(Collection<Payment> payments) {
        for (var p : payments) {
            if (p.getSenderAccount() != null) {
                return p.getSenderAccount();
            }
        }
        return null;
    }

    private Address getSenderAddress(Collection<Payment> payments) {
        for (var p : payments) {
            if (p.getSenderAddress() != null) {
                return p.getSenderAddress();
            }
        }
        return null;
    }

    private void showFeeEdit() {
        var ledger = PaymentUtils.distinctLedgers(payments);
        for (var l : ledger) {
            showFeeEdit(l, payments.length == 0 ? null : payments[0].getFeeSmallestUnit());
        }
    }

    private void showFeeEdit(Ledger l, Long currentFee) {
        var fees = l.getFeeSuggestion();

        var pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setMinimumSize(new Dimension(Integer.MAX_VALUE, 80));
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        pnl.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));

        var group = new ButtonGroup();
        var rdoLow = new JRadioButton("Low");
        group.add(rdoLow);
        var rdoMedium = new JRadioButton("Medium");
        group.add(rdoMedium);
        var rdoHigh = new JRadioButton("High");
        group.add(rdoHigh);

        rdoLow.setSelected(currentFee == null || fees.getLow() == currentFee);
        rdoMedium.setSelected(fees.getMedium() == currentFee);
        rdoHigh.setSelected(fees.getHigh() == currentFee);

        pnl.add(createFeeRow(l, rdoLow, fees.getLow()));
        pnl.add(createFeeRow(l, rdoMedium, fees.getMedium()));
        pnl.add(createFeeRow(l, rdoHigh, fees.getHigh()));

        var optionPane = new JOptionPane();
        optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
        var ret = JOptionPane.showOptionDialog(this, pnl, "Fee per transaction",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"OK", "Cancel"}, "OK");
        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        var affectedPayments = new ArrayList<Payment>();
        for (var p : payments) {
            if (p.getLedger().getId().textId().equals(l.getId().textId())) {
                affectedPayments.add(p);
            }
        }

        var fr = new FeeRefresher(affectedPayments.toArray(new Payment[0]));
        if (rdoLow.isSelected()) {
            fr.refresh(fees.getLow());
        }
        if (rdoMedium.isSelected()) {
            fr.refresh(fees.getMedium());
        }
        if (rdoHigh.isSelected()) {
            fr.refresh(fees.getHigh());
        }

        refreshTotalFee();
    }

    private Component createFeeRow(Ledger l, JRadioButton rdo, long fee) {
        var pnl = new JPanel();
        var layout = new SpringLayout();
        pnl.setLayout(layout);

        pnl.add(rdo);
        layout.putConstraint(SpringLayout.WEST, rdo, 0, SpringLayout.WEST, pnl);

        var lblFee = new JLabel(formatLedgerAmount(l, fee));
        pnl.add(lblFee);
        layout.putConstraint(SpringLayout.EAST, lblFee, 0, SpringLayout.EAST, pnl);

        return pnl;
    }

    private String formatLedgerAmount(Ledger l, Long amtSmallestUnit) {
        return MoneyFormatter.formatLedger(l.convertToNativeCcyAmount(amtSmallestUnit), l.getNativeCcySymbol());
    }

    private void startTimeoutCountdown() {
        var scheduler = Executors.newScheduledThreadPool(1);
        var runnable = new Runnable() {
            int countdown = 60;

            public void run() {
                cmdSend.setText(String.format("Send [%ss] ", countdown));

                countdown--;
                if (countdown < 0) {
                    cmdSend.setText("Send");
                    // Ensure user doesn't use outdated exchange rates (ex. let UI open for an hour).
                    cmdSend.setEnabled(false);
                    scheduler.shutdown();
                }
            }
        };
        scheduler.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
    }

    private void onOk() {
        setDialogAccepted(true);
        close();
    }

    private void close() {
        dispose();
    }

    private static int getNorthPad(int line) {
        final var lineHeight = 70;
        return line * lineHeight;
    }

    private void setDialogAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isDialogAccepted() {
        return accepted;
    }
}
