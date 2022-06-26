package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.DateTimeConvert;
import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoAggregator;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import com.radynamics.CryptoIso20022Interop.iso20022.AmountFormatter;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PaymentDetailForm extends JDialog {
    private Payment payment;
    private PaymentValidator validator;
    private ExchangeRateProvider exchangeRateProvider;
    private final WalletInfoAggregator walletInfoAggregator;

    private SpringLayout panel1Layout;
    private JPanel pnlContent;
    private Component anchorComponentTopLeft;
    private boolean paymentChanged;
    private JLabel lblLedgerAmount;
    private JLabel lblAmountText;

    public PaymentDetailForm(Payment payment, PaymentValidator validator, ExchangeRateProvider exchangeRateProvider) {
        if (payment == null) throw new IllegalArgumentException("Parameter 'payment' cannot be null");
        if (validator == null) throw new IllegalArgumentException("Parameter 'validator' cannot be null");
        if (exchangeRateProvider == null) throw new IllegalArgumentException("Parameter 'exchangeRateProvider' cannot be null");
        this.payment = payment;
        this.validator = validator;
        this.exchangeRateProvider = exchangeRateProvider;
        this.walletInfoAggregator = new WalletInfoAggregator(payment.getLedger().getInfoProvider());

        setupUI();
    }

    private void setupUI() {
        setTitle("Payment detail");
        setIconImage(Utils.getProductIcon());

        var al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        };
        getRootPane().registerKeyboardAction(al, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

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
        JPanel panel3 = new JPanel();
        var panel3Layout = new SpringLayout();
        panel3.setLayout(panel3Layout);

        panel1.add(pnlContent);

        pnlMain.add(panel0);
        pnlMain.add(panel1);
        pnlMain.add(panel3);

        panel0.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel0.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel0.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));

        {
            var lbl = new JLabel();
            lbl.setText(getTitle());
            lbl.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
            lbl.setOpaque(true);
            panel0.add(lbl);
        }

        {
            int row = 0;
            {
                var secondLine = new JPanel();
                secondLine.setLayout(new BoxLayout(secondLine, BoxLayout.X_AXIS));
                lblAmountText = new JLabel();
                lblLedgerAmount = Utils.formatSecondaryInfo(new JLabel());
                refreshAmountsText();
                secondLine.add(lblLedgerAmount);
                {
                    var lbl = formatSecondLineLinkLabel(Utils.createLinkLabel(this, "edit..."));
                    lbl.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 1) {
                                showExchangeRateEdit();
                            }
                        }
                    });
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                    secondLine.add(lbl);
                }
                anchorComponentTopLeft = createRow(row++, "Amount:", lblAmountText, secondLine, false);
            }
            {
                var lbl = new WalletLabel();
                lbl.setWallet(payment.getSenderWallet())
                        .setLedger(payment.getLedger())
                        .setAccount(payment.getSenderAccount())
                        .setAddress(payment.getSenderAddress())
                        .setWalletInfoAggregator(walletInfoAggregator);
                createRow(row++, "Sender:", lbl, null, false);
            }
            {
                var lbl = new WalletLabel();
                lbl.setWallet(payment.getReceiverWallet())
                        .setLedger(payment.getLedger())
                        .setAccount(payment.getReceiverAccount())
                        .setAddress(payment.getReceiverAddress())
                        .setWalletInfoAggregator(walletInfoAggregator);
                createRow(row++, "Receiver:", lbl, null, false);
            }
            {
                JLabel secondLine = null;
                if (payment.getId() != null) {
                    secondLine = formatSecondLineLinkLabel(Utils.createLinkLabel(this, "show ledger transaction..."));
                    secondLine.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 1) {
                                showLedgerTransaction();
                            }
                        }
                    });
                }
                createRow(row++, "Booked:", payment.getBooked() == null ? "unknown" : Utils.createFormatDate().format(DateTimeConvert.toUserTimeZone(payment.getBooked())), secondLine);
            }
            {
                var sb = new StringBuilder();
                for (var ref : payment.getStructuredReferences()) {
                    sb.append(String.format("%s\n", ref.getUnformatted()));
                }
                var txt = createTextArea(1, Utils.removeEndingLineSeparator(sb.toString()));
                createRow(row++, "References:", txt, null);
            }
            {
                var sb = new StringBuilder();
                for (var m : payment.getMessages()) {
                    sb.append(String.format("%s\n", m));
                }
                var txt = createTextArea(3, Utils.removeEndingLineSeparator(sb.toString()));
                createRow(row++, "Messages:", txt, null);
                row++;
            }
            {
                var sb = new StringBuilder();
                if (payment.getTransmissionError() != null) {
                    sb.append(String.format("%s\n", payment.getTransmissionError().getMessage()));
                }
                for (var vr : validator.validate(payment)) {
                    sb.append(String.format("- [%s] %s\n", vr.getStatus().name(), vr.getMessage()));
                }
                var pnl = new JPanel();
                pnl.setLayout(new BorderLayout());
                pnl.add(createTextArea(3, sb.length() == 0 ? "" : Utils.removeEndingLineSeparator(sb.toString())));
                createRow(row++, "Issues:", pnl, null, true);
            }
        }
        {
            var cmd = new JButton("Close");
            cmd.setPreferredSize(new Dimension(150, 35));
            cmd.addActionListener(e -> {
                close();
            });
            panel3Layout.putConstraint(SpringLayout.EAST, cmd, 0, SpringLayout.EAST, panel3);
            panel3Layout.putConstraint(SpringLayout.SOUTH, cmd, 0, SpringLayout.SOUTH, panel3);
            panel3.add(cmd);
        }
    }

    private void refreshAmountsText() {
        lblAmountText.setText(AmountFormatter.formatAmtWithCcy(payment));

        var amtLedgerText = MoneyFormatter.formatLedger(payment.getAmountLedgerUnit(), payment.getLedgerCcy());
        if (payment.getExchangeRate() == null) {
            lblLedgerAmount.setText(String.format("%s, missing exchange rate", amtLedgerText));
            return;
        }

        var fxRateText = "unknown";
        var fxRateAtText = "unknown";
        if (!payment.isAmountUnknown()) {
            fxRateText = Utils.createFormatLedger().format(payment.getExchangeRate().getRate());
            fxRateAtText = Utils.createFormatDate().format(DateTimeConvert.toUserTimeZone(payment.getExchangeRate().getPointInTime()));
        }
        lblLedgerAmount.setText(String.format("%s with exchange rate %s at %s", amtLedgerText, fxRateText, fxRateAtText));
    }

    private void showExchangeRateEdit() {
        var rate = payment.getExchangeRate() == null ? ExchangeRate.Undefined(payment.createCcyPair()) : payment.getExchangeRate();

        var frm = new ExchangeRatesForm(exchangeRateProvider, new ExchangeRate[]{rate}, rate.getPointInTime());
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(400, 300);
        frm.setModal(true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);

        if (!frm.isDialogAccepted()) {
            return;
        }

        payment.setExchangeRate(rate.isUndefined() ? null : rate);
        payment.refreshAmounts();
        refreshAmountsText();
        setPaymentChanged(true);
    }

    private void showLedgerTransaction() {
        payment.getLedger().getTransactionLookupProvider().open(payment.getId());
    }

    private JScrollPane createTextArea(int rows, String text) {
        var txt = new JTextArea(rows, 39);
        txt.setLineWrap(true);
        txt.setEditable(false);
        txt.setText(text);
        txt.setCaretPosition(0);
        return new JScrollPane(txt);
    }

    private void close() {
        dispose();
    }

    private Component createRow(int row, String labelText, Component firstLine, String contentSecondLine) {
        JLabel secondLine = null;
        if (contentSecondLine != null) {
            secondLine = new JLabel(contentSecondLine);
            Utils.formatSecondaryInfo(secondLine);
        }
        return createRow(row, labelText, firstLine, secondLine, false);
    }

    private Component createRow(int row, String labelText, String contentFirstLine, Component secondLine) {
        return createRow(row, labelText, new JLabel(contentFirstLine), secondLine, false);
    }

    private Component createRow(int row, String labelText, Component firstLine, Component secondLine, boolean growBottomRight) {
        var lbl = new JLabel(labelText);
        panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnlContent);
        panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(row), SpringLayout.NORTH, pnlContent);
        lbl.setOpaque(true);
        pnlContent.add(lbl);

        panel1Layout.putConstraint(SpringLayout.WEST, firstLine, 50, SpringLayout.EAST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
        panel1Layout.putConstraint(SpringLayout.NORTH, firstLine, getNorthPad(row), SpringLayout.NORTH, pnlContent);
        if (growBottomRight) {
            panel1Layout.putConstraint(SpringLayout.EAST, pnlContent, 0, SpringLayout.EAST, firstLine);
            panel1Layout.putConstraint(SpringLayout.SOUTH, pnlContent, 0, SpringLayout.SOUTH, firstLine);
        }
        pnlContent.add(firstLine);

        if (secondLine != null) {
            panel1Layout.putConstraint(SpringLayout.WEST, secondLine, 50, SpringLayout.EAST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
            panel1Layout.putConstraint(SpringLayout.NORTH, secondLine, getNorthPad(row) + 13, SpringLayout.NORTH, pnlContent);
            pnlContent.add(secondLine);
        }

        return lbl;
    }

    private JLabel formatSecondLineLinkLabel(JLabel lbl) {
        lbl.putClientProperty("FlatLaf.styleClass", "small");
        return lbl;
    }

    private static int getNorthPad(int line) {
        final var lineHeight = 30;
        return line * lineHeight;
    }

    private void setPaymentChanged(boolean changed) {
        this.paymentChanged = changed;
    }

    public boolean getPaymentChanged() {
        return paymentChanged;
    }
}
