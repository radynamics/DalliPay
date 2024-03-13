package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SendConfirmationForm extends JDialog {
    private final Ledger ledger;
    private final Payment[] payments;
    private final ExchangeRateProvider provider;
    private int totalPaymentCount;
    private final FormAcceptCloseHandler formAcceptCloseHandler = new FormAcceptCloseHandler(this);

    private JPanel pnlContent;
    private boolean accepted;
    private JButton cmdSend;
    private JLabel lblFee;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    private static final int ENTRY_VERTICAL_SPACING = 7;
    private static final int ENTRY_HEIGHT = 45;

    public SendConfirmationForm(Ledger ledger, Payment[] payments, ExchangeRateProvider provider, int totalPaymentCount) {
        this.ledger = ledger;
        this.payments = payments;
        this.provider = provider;
        this.totalPaymentCount = totalPaymentCount;
        setupUI();
    }

    private void setupUI() {
        setTitle(res.getString("title"));
        setIconImage(Utils.getProductIcon());

        formAcceptCloseHandler.configure();
        formAcceptCloseHandler.addFormActionListener(new FormActionListener() {
            @Override
            public void onAccept() {
                acceptDialog();
            }

            @Override
            public void onCancel() {
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
        pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
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

        var sp = new JScrollPane(pnlContent);
        sp.setBorder(BorderFactory.createEmptyBorder());
        panel1.add(sp);

        pnlMain.add(panel0);
        pnlMain.add(panel1);
        pnlMain.add(panel2);
        pnlMain.add(panel3);

        panel0.setMinimumSize(new Dimension(Integer.MAX_VALUE, 110));
        panel0.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        panel0.setPreferredSize(new Dimension(Integer.MAX_VALUE, 110));
        panel2.setMinimumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel2.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
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
                lbl.setText(res.getString("pleaseReview"));
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
                        var lbl = new JLabel(String.format(res.getString("selectXofY"), payments.length, totalPaymentCount));
                        lbl.setOpaque(true);
                        pnlInfo.add(lbl);
                    }
                }
            }
        }

        {
            var sendingWallets = PaymentUtils.distinctSendingWallets(payments);
            pnlContent.setPreferredSize(new Dimension(100, sendingWallets.size() * (ENTRY_HEIGHT + ENTRY_VERTICAL_SPACING)));
            for (var w : sendingWallets) {
                var payments = PaymentUtils.fromSender(w, this.payments);
                if (payments.size() == 0) {
                    continue;
                }
                createRow(w, getSenderAccount(payments), getSenderAddress(payments), payments);
            }
        }
        {
            var lblFeeText = new JLabel(res.getString("totalExpectedFee"));
            panel2Layout.putConstraint(SpringLayout.WEST, lblFeeText, 0, SpringLayout.WEST, pnlFeeContent);
            panel2Layout.putConstraint(SpringLayout.NORTH, lblFeeText, 0, SpringLayout.NORTH, pnlFeeContent);
            pnlFeeContent.add(lblFeeText);

            lblFee = new JLabel();
            refreshTotalFee();
            panel2Layout.putConstraint(SpringLayout.WEST, lblFee, 10, SpringLayout.EAST, lblFeeText);
            panel2Layout.putConstraint(SpringLayout.NORTH, lblFee, 0, SpringLayout.NORTH, pnlFeeContent);
            pnlFeeContent.add(lblFee);

            var lbl3 = Utils.createLinkLabel(pnlMain, res.getString("edit"));
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

            var lbl4 = Utils.createLinkLabel(pnlMain, res.getString("explain"));
            panel2Layout.putConstraint(SpringLayout.WEST, lbl4, 10, SpringLayout.EAST, lbl3);
            panel2Layout.putConstraint(SpringLayout.NORTH, lbl4, 0, SpringLayout.NORTH, pnlFeeContent);
            lbl4.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        showFeeDetails();
                    }
                }
            });
            pnlFeeContent.add(lbl4);
        }
        {
            var pnl = new JPanel();
            panel3Layout.putConstraint(SpringLayout.EAST, pnl, 0, SpringLayout.EAST, panel3);
            panel3Layout.putConstraint(SpringLayout.SOUTH, pnl, 0, SpringLayout.SOUTH, panel3);
            panel3.add(pnl);
            {
                cmdSend = new JButton(res.getString("send"));
                cmdSend.setPreferredSize(new Dimension(150, 35));
                cmdSend.addActionListener(e -> formAcceptCloseHandler.accept());
                pnl.add(cmdSend);
            }
            {
                var cmd = new JButton(res.getString("cancel"));
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> formAcceptCloseHandler.close());
                pnl.add(cmd);
            }
        }

        startTimeoutCountdown();
    }

    private void acceptDialog() {
        setDialogAccepted(true);
    }

    private void refreshTotalFee() {
        lblFee.setText(PaymentUtils.totalFeesText(payments, provider));
    }

    private void createRow(Wallet sendingWallet, Account senderAccount, Address senderAddress, ArrayList<Payment> payments) {
        var panel1Layout = new SpringLayout();
        var pnl = new JPanel();
        pnl.setLayout(panel1Layout);
        pnlContent.add(pnl);
        pnl.setPreferredSize(new Dimension(Integer.MAX_VALUE, ENTRY_HEIGHT));
        pnl.setMinimumSize(new Dimension(Integer.MAX_VALUE, ENTRY_HEIGHT));

        pnlContent.add(Box.createRigidArea(new Dimension(0, ENTRY_VERTICAL_SPACING)));

        {
            var lbl = new WalletLabel();
            lbl.setWallet(sendingWallet)
                    .setLedger(ledger)
                    .setAccount(senderAccount)
                    .setAddress(senderAddress)
                    .setWalletInfoAggregator(new WalletInfoAggregator(ledger.getInfoProvider()))
                    .setInfoLineCount(2);
            panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnl);
            panel1Layout.putConstraint(SpringLayout.NORTH, lbl, 0, SpringLayout.NORTH, pnl);
            pnl.add(lbl);
        }

        pnl.add(Box.createHorizontalGlue());

        var pnlAmounts = new JPanel();
        pnlAmounts.setLayout(new BoxLayout(pnlAmounts, BoxLayout.Y_AXIS));
        pnlAmounts.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panel1Layout.putConstraint(SpringLayout.NORTH, pnlAmounts, 0, SpringLayout.NORTH, pnl);
        panel1Layout.putConstraint(SpringLayout.EAST, pnlAmounts, 0, SpringLayout.EAST, pnl);
        pnl.add(pnlAmounts);
        {
            var p = new JPanel();
            var layout = new BoxLayout(p, BoxLayout.X_AXIS);
            p.setLayout(layout);
            pnlAmounts.add(p);
            p.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

            p.add(new JLabel(PaymentUtils.sumString(payments, true)));
            p.add(Box.createRigidArea(new Dimension(5, 0)));
            p.add(new JLabel(String.format("(%s " + res.getString("payments") + ")", payments.size())));
        }
        {
            var layout = new FlowLayout(FlowLayout.RIGHT, 5, 0);
            var p = new JPanel();
            p.setLayout(layout);
            pnlAmounts.add(p);

            var sums = Money.sort(Money.removeZero(PaymentUtils.sumLedgerUnit(payments).sum()));
            for (var amount : sums) {
                var lbl = new MoneyLabel(ledger);
                lbl.setAmount(amount);
                lbl.setIssuerVisible(false);
                lbl.formatAsSecondaryInfo();
                p.add(lbl);
            }
        }
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
        var fr = new FeeRefresher(payments);
        var feeEdit = new FeeEdit(fr);
        if (feeEdit.showDialog(this) != JOptionPane.OK_OPTION) {
            return;
        }

        if (feeEdit.isLowSelected()) {
            fr.setAllLow();
        }
        if (feeEdit.isMediumSelected()) {
            fr.setAllMedium();
        }
        if (feeEdit.isHighSelected()) {
            fr.setAllHigh();
        }

        refreshTotalFee();
    }

    private void showFeeDetails() {
        var sb = new StringBuilder();

        for (var i = 0; i < payments.length; i++) {
            if (sb.length() > 0) {
                sb.append(System.lineSeparator());
            }
            var p = payments[i];
            var paymentHeader = p.getReceiverAddress() == null
                    ? PaymentFormatter.singleLineText(p.getSenderAccount(), p.getSenderAddress())
                    : AddressFormatter.formatSingleLine(p.getReceiverAddress());
            sb.append(String.format("%s. %s, %s%s", i + 1, paymentHeader, AmountFormatter.formatAmtWithCcy(p), System.lineSeparator()));
            for (var fee : p.getFees()) {
                sb.append(String.format("%s: %s%s", MoneyFormatter.formatLedger(fee.getAmount()), FeeHelper.getText(fee.getType()), System.lineSeparator()));
            }
        }

        var textArea = new JTextArea(sb.toString());
        textArea.setColumns(50);
        textArea.setRows(20);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), res.getString("fees.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void startTimeoutCountdown() {
        var scheduler = Executors.newScheduledThreadPool(1);
        var runnable = new Runnable() {
            int countdown = 60;

            public void run() {
                cmdSend.setText(String.format(res.getString("send") + " [%ss] ", countdown));

                countdown--;
                if (countdown < 0) {
                    cmdSend.setText(res.getString("send"));
                    // Ensure user doesn't use outdated exchange rates (ex. let UI open for an hour).
                    cmdSend.setEnabled(false);
                    scheduler.shutdown();
                }
            }
        };
        scheduler.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
    }

    private void setDialogAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isDialogAccepted() {
        return accepted;
    }
}
