package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.iso20022.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class PaymentDetailForm extends JDialog {
    private Payment payment;
    private SpringLayout panel1Layout;
    private JPanel panel1;
    private Component anchorComponentTopLeft;

    public PaymentDetailForm(Payment payment) {
        if (payment == null) throw new IllegalArgumentException("Parameter 'payment' cannot be null");
        this.payment = payment;

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
        panel1 = new JPanel();
        panel1.setBorder(innerBorder);
        panel1Layout = new SpringLayout();
        panel1.setLayout(panel1Layout);
        JPanel panel3 = new JPanel();
        panel3.setBorder(innerBorder);
        var panel3Layout = new SpringLayout();
        panel3.setLayout(panel3Layout);

        pnlMain.add(panel0);
        pnlMain.add(panel1);
        pnlMain.add(panel3);

        panel0.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel0.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel0.setPreferredSize(new Dimension(500, 50));
        panel1.setMinimumSize(new Dimension(Integer.MAX_VALUE, 350));
        panel1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        panel1.setPreferredSize(new Dimension(500, 350));
        panel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setPreferredSize(new Dimension(500, 45));

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
                var amtText = AmountFormatter.formatAmt(payment);
                var amtLedgerText = Utils.createFormatLedger().format(payment.getLedger().convertToNativeCcyAmount(payment.getLedgerAmountSmallestUnit()));
                var fxRateText = "unknown";
                var fxRateAtText = "unknown";
                if (!payment.isAmountUnknown()) {
                    fxRateText = Utils.createFormatLedger().format(payment.getExchangeRate().getRate());
                    fxRateAtText = Utils.createFormatDate().format(payment.getExchangeRate().getPointInTime());
                }
                anchorComponentTopLeft = createRow(row++, "Amount:",
                        String.format("%s %s", amtText, payment.getFiatCcy() == null ? "" : payment.getFiatCcy()),
                        String.format("%s %s with exchange rate %s at %s", amtLedgerText, payment.getLedgerCcy(), fxRateText, fxRateAtText));
            }
            createRow(row++, "Sender:", getActorText(payment.getSenderAccount(), payment.getSenderAddress()), getWalletText(payment.getSenderWallet()));
            createRow(row++, "Receiver:", getActorText(payment.getReceiverAccount(), payment.getReceiverAddress()), getWalletText(payment.getReceiverWallet()));
            {
                var sb = new StringBuilder();
                for (var ref : payment.getStructuredReferences()) {
                    sb.append(String.format("%s\n", ref.getUnformatted()));
                }
                var txt = new JTextArea(getTextOrDefault(sb));
                txt.setPreferredSize(new Dimension(300, txt.getPreferredSize().height));
                txt.setEnabled(false);
                createRow(row++, "References:", txt, null);
            }
            {
                var sb = new StringBuilder();
                for (var m : payment.getMessages()) {
                    sb.append(String.format("%s\n", m));
                }
                var txt = new JTextArea(getTextOrDefault(sb));
                txt.setPreferredSize(new Dimension(300, txt.getPreferredSize().height));
                txt.setEnabled(false);
                createRow(row++, "Messages:", txt, null);
            }
        }
        {
            var cmd = new JButton("Close");
            cmd.setPreferredSize(new Dimension(150, 35));
            cmd.addActionListener(e -> {
                close();
            });
            panel3Layout.putConstraint(SpringLayout.EAST, cmd, 0, SpringLayout.EAST, panel3);
            panel3.add(cmd);
        }
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
        return createRow(row, labelText, contentFirstLine, null);
    }

    private Component createRow(int row, String labelText, String contentFirstLine, String contentSecondLine) {
        return createRow(row, labelText, new JLabel(contentFirstLine), contentSecondLine);
    }

    private Component createRow(int row, String labelText, Component firstLine, String contentSecondLine) {
        var lbl = new JLabel(labelText);
        panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
        panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(row), SpringLayout.NORTH, panel1);
        lbl.setOpaque(true);
        panel1.add(lbl);

        panel1Layout.putConstraint(SpringLayout.WEST, firstLine, 50, SpringLayout.EAST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
        panel1Layout.putConstraint(SpringLayout.NORTH, firstLine, getNorthPad(row), SpringLayout.NORTH, panel1);
        panel1.add(firstLine);

        if (contentSecondLine != null) {
            var lbl3 = new JLabel(contentSecondLine);
            lbl3.putClientProperty("FlatLaf.styleClass", "small");
            lbl3.setForeground(Consts.ColorSmallInfo);
            panel1Layout.putConstraint(SpringLayout.WEST, lbl3, 50, SpringLayout.EAST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
            panel1Layout.putConstraint(SpringLayout.NORTH, lbl3, getNorthPad(row) + 13, SpringLayout.NORTH, panel1);
            panel1.add(lbl3);
        }

        return lbl;
    }

    private static int getNorthPad(int line) {
        final var lineHeight = 30;
        return line * lineHeight;
    }
}
