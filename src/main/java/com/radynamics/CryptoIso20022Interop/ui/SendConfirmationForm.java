package com.radynamics.CryptoIso20022Interop.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SendConfirmationForm extends JDialog {
    private final Payment[] payments;
    private final ExchangeRateProvider provider;
    private int totalPaymentCount;
    private final FormAcceptCloseHandler formAcceptCloseHandler = new FormAcceptCloseHandler(this);

    private JPanel pnlContent;
    private boolean accepted;
    private JButton cmdSend;
    private JLabel lblFee;

    private static final int ENTRY_VERTICAL_SPACING = 7;
    private static final int ENTRY_HEIGHT = 45;

    public SendConfirmationForm(Payment[] payments, ExchangeRateProvider provider, int totalPaymentCount) {
        this.payments = payments;
        this.provider = provider;
        this.totalPaymentCount = totalPaymentCount;
        setupUI();
    }

    private void setupUI() {
        setTitle("Confirm Payments");
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
            var lblFeeText = new JLabel("Total expected Transaction fee");
            panel2Layout.putConstraint(SpringLayout.WEST, lblFeeText, 0, SpringLayout.WEST, pnlFeeContent);
            panel2Layout.putConstraint(SpringLayout.NORTH, lblFeeText, 0, SpringLayout.NORTH, pnlFeeContent);
            pnlFeeContent.add(lblFeeText);

            lblFee = new JLabel();
            refreshTotalFee();
            panel2Layout.putConstraint(SpringLayout.WEST, lblFee, 10, SpringLayout.EAST, lblFeeText);
            panel2Layout.putConstraint(SpringLayout.NORTH, lblFee, 0, SpringLayout.NORTH, pnlFeeContent);
            pnlFeeContent.add(lblFee);

            var lbl3 = Utils.createLinkLabel(pnlMain, "edit...");
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

            var lbl4 = Utils.createLinkLabel(pnlMain, "explain...");
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
                cmdSend = new JButton("Send");
                cmdSend.setPreferredSize(new Dimension(150, 35));
                cmdSend.addActionListener(e -> formAcceptCloseHandler.accept());
                pnl.add(cmdSend);
            }
            {
                var cmd = new JButton("Cancel");
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
        Ledger l = payments.get(0).getLedger();

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
                    .setLedger(l)
                    .setAccount(senderAccount)
                    .setAddress(senderAddress)
                    .setWalletInfoAggregator(new WalletInfoAggregator(l.getInfoProvider()))
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

            p.add(new JLabel(PaymentUtils.sumString(payments)));
            p.add(Box.createRigidArea(new Dimension(5, 0)));
            p.add(new JLabel(String.format("(%s payments)", payments.size())));
        }
        {
            var layout = new FlowLayout(FlowLayout.RIGHT, 5, 0);
            var p = new JPanel();
            p.setLayout(layout);
            pnlAmounts.add(p);

            var sums = Money.sort(Money.removeZero(PaymentUtils.sumLedgerUnit(payments).sum()));
            for (var amount : sums) {
                var lbl = new MoneyLabel(l);
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
        var ledger = PaymentUtils.distinctLedgers(payments);
        for (var l : ledger) {
            var ledgerTransactionFee = payments.length == 0
                    ? null
                    : FeeHelper.get(payments[0].getFees(), FeeType.LedgerTransactionFee).orElse(null);
            showFeeEdit(l, ledgerTransactionFee);
        }
    }

    private void showFeeEdit(Ledger l, Money currentFee) {
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

        rdoLow.setSelected(currentFee == null || fees.getLow().equals(currentFee));
        rdoMedium.setSelected(fees.getMedium().equals(currentFee));
        rdoHigh.setSelected(fees.getHigh().equals(currentFee));

        pnl.add(createFeeRow(rdoLow, fees.getLow()));
        pnl.add(createFeeRow(rdoMedium, fees.getMedium()));
        pnl.add(createFeeRow(rdoHigh, fees.getHigh()));

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

    private Component createFeeRow(JRadioButton rdo, Money fee) {
        var pnl = new JPanel();
        var layout = new SpringLayout();
        pnl.setLayout(layout);

        pnl.add(rdo);
        layout.putConstraint(SpringLayout.WEST, rdo, 0, SpringLayout.WEST, pnl);

        var lblFee = new JLabel(MoneyFormatter.formatLedger(fee));
        pnl.add(lblFee);
        layout.putConstraint(SpringLayout.EAST, lblFee, 0, SpringLayout.EAST, pnl);

        return pnl;
    }

    private void showFeeDetails() {
        var sb = new StringBuilder();

        for (var i = 0; i < payments.length; i++) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            var p = payments[i];
            var paymentHeader = p.getReceiverAddress() == null
                    ? AccountFormatter.format(p.getReceiverAccount())
                    : AddressFormatter.formatSingleLine(p.getReceiverAddress());
            sb.append(String.format("%s. %s, %s\n", i + 1, paymentHeader, AmountFormatter.formatAmtWithCcy(p)));
            for (var fee : p.getFees()) {
                sb.append(String.format("%s: %s\n", MoneyFormatter.formatLedger(fee.getAmount()), FeeHelper.getText(fee.getType())));
            }
        }

        var textArea = new JTextArea(sb.toString());
        textArea.setColumns(50);
        textArea.setRows(20);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Fees", JOptionPane.INFORMATION_MESSAGE);
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

    private void setDialogAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isDialogAccepted() {
        return accepted;
    }
}
