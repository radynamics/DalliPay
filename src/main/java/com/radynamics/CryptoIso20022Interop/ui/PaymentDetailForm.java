package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.iso20022.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class PaymentDetailForm extends JDialog {
    private Payment payment;
    private SpringLayout panel1Layout;
    private JPanel pnlContent;
    private Component anchorComponentTopLeft;
    private PaymentValidator validator;

    public PaymentDetailForm(Payment payment, PaymentValidator validator) {
        if (payment == null) throw new IllegalArgumentException("Parameter 'payment' cannot be null");
        if (validator == null) throw new IllegalArgumentException("Parameter 'validator' cannot be null");
        this.payment = payment;
        this.validator = validator;

        setupUI();
    }

    private void setupUI() {
        setTitle("Payment detail");

        try {
            setIconImage(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("img/productIcon.png"))).getImage());
        } catch (IOException e) {
            ExceptionDialog.show(this, e);
        }

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
                var amtText = AmountFormatter.formatAmtWithCcy(payment);
                var amtLedgerText = MoneyFormatter.formatLedger(payment.getLedger().convertToNativeCcyAmount(payment.getLedgerAmountSmallestUnit()), payment.getLedgerCcy());
                var fxRateText = "unknown";
                var fxRateAtText = "unknown";
                if (!payment.isAmountUnknown()) {
                    fxRateText = Utils.createFormatLedger().format(payment.getExchangeRate().getRate());
                    fxRateAtText = Utils.createFormatDate().format(payment.getExchangeRate().getPointInTime());
                }
                anchorComponentTopLeft = createRow(row++, "Amount:", amtText,
                        String.format("%s with exchange rate %s at %s", amtLedgerText, fxRateText, fxRateAtText));
            }
            {
                var secondLineText = getWalletText(payment.getSenderWallet());
                if (payment.getSenderWallet() != null) {
                    var amtSmallestUnit = payment.getSenderWallet().getLedgerBalanceSmallestUnit();
                    if (amtSmallestUnit != null) {
                        var balance = payment.getLedger().convertToNativeCcyAmount(amtSmallestUnit.longValue());
                        secondLineText = String.format("%s (%s)", secondLineText, MoneyFormatter.formatLedger(balance, payment.getLedgerCcy()));
                    }
                }
                createRow(row++, "Sender:", getActorText(payment.getSenderAccount(), payment.getSenderAddress()), secondLineText);
            }
            createRow(row++, "Receiver:", getActorText(payment.getReceiverAccount(), payment.getReceiverAddress()), getWalletText(payment.getReceiverWallet()));
            {
                JLabel secondLine = null;
                if (payment.getId() != null) {
                    secondLine = Utils.createLinkLabel(this, "show ledger transaction...");
                    secondLine.putClientProperty("FlatLaf.styleClass", "small");
                    secondLine.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 1) {
                                showLedgerTransaction();
                            }
                        }
                    });
                }
                createRow(row++, "Booked:", payment.getBooked() == null ? "unknown" : Utils.createFormatDate().format(payment.getBooked()), secondLine);
            }
            {
                var sb = new StringBuilder();
                for (var ref : payment.getStructuredReferences()) {
                    sb.append(String.format("%s\n", ref.getUnformatted()));
                }
                var txt = createTextArea(1, sb.toString());
                createRow(row++, "References:", txt, (String) null);
            }
            {
                var sb = new StringBuilder();
                for (var m : payment.getMessages()) {
                    sb.append(String.format("%s\n", m));
                }
                var txt = createTextArea(1, sb.toString());
                createRow(row++, "Messages:", txt, (String) null);
            }
            {
                var sb = new StringBuilder();
                if (payment.getTransmissionError() != null) {
                    sb.append(String.format("%s\n", payment.getTransmissionError().getMessage()));
                }
                for (var vr : validator.validate(payment)) {
                    sb.append(String.format("- [%s] %s\n", vr.getStatus().name(), vr.getMessage()));
                }
                var txt = createTextArea(3, sb.length() == 0 ? "none" : sb.toString());
                createRow(row++, "Issues:", txt, (String) null);
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

    private String getTextOrDefault(StringBuilder sb) {
        return sb.length() == 0 ? "none" : sb.toString();
    }

    private String getWalletText(Wallet wallet) {
        return wallet == null ? "Missing Wallet" : wallet.getPublicKey();
    }

    private String getActorText(Account account, Address address) {
        var sb = new StringBuilder();

        if (address != null) {
            sb.append(AddressFormatter.formatSingleLine(address));
        }

        if (account == null) {
            return sb.toString();
        }

        var template = sb.length() == 0 ? "%s" : " (%s)";
        sb.append(String.format(template, AccountFormatter.format(account)));

        return sb.toString();
    }

    private Component createRow(int row, String labelText, String contentFirstLine) {
        return createRow(row, labelText, contentFirstLine, (String) null);
    }

    private Component createRow(int row, String labelText, String contentFirstLine, String contentSecondLine) {
        return createRow(row, labelText, new JLabel(contentFirstLine), contentSecondLine);
    }

    private Component createRow(int row, String labelText, Component firstLine, String contentSecondLine) {
        JLabel secondLine = null;
        if (contentSecondLine != null) {
            secondLine = new JLabel(contentSecondLine);
            secondLine.putClientProperty("FlatLaf.styleClass", "small");
            secondLine.setForeground(Consts.ColorSmallInfo);
        }
        return createRow(row, labelText, firstLine, secondLine);
    }

    private Component createRow(int row, String labelText, String contentFirstLine, Component secondLine) {
        return createRow(row, labelText, new JLabel(contentFirstLine), secondLine);
    }

    private Component createRow(int row, String labelText, Component firstLine, Component secondLine) {
        var lbl = new JLabel(labelText);
        panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnlContent);
        panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(row), SpringLayout.NORTH, pnlContent);
        lbl.setOpaque(true);
        pnlContent.add(lbl);

        panel1Layout.putConstraint(SpringLayout.WEST, firstLine, 50, SpringLayout.EAST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
        panel1Layout.putConstraint(SpringLayout.NORTH, firstLine, getNorthPad(row), SpringLayout.NORTH, pnlContent);
        pnlContent.add(firstLine);

        if (secondLine != null) {
            panel1Layout.putConstraint(SpringLayout.WEST, secondLine, 50, SpringLayout.EAST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
            panel1Layout.putConstraint(SpringLayout.NORTH, secondLine, getNorthPad(row) + 13, SpringLayout.NORTH, pnlContent);
            pnlContent.add(secondLine);
        }

        return lbl;
    }

    private static int getNorthPad(int line) {
        final var lineHeight = 30;
        return line * lineHeight;
    }
}
