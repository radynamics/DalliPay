package com.radynamics.CryptoIso20022Interop.ui;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.VersionController;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.PaymentConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.Camt054Writer;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.CamtConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.PaymentValidator;
import com.radynamics.CryptoIso20022Interop.transformation.TransactionTranslator;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.ui.paymentTable.Actor;
import com.radynamics.CryptoIso20022Interop.ui.paymentTable.PaymentTable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class ReceiveForm extends JFrame {
    private TransformInstruction transformInstruction;
    private CurrencyConverter currencyConverter;
    private final VersionController versionController = new VersionController();

    private PaymentTable table;
    private WalletField txtInput;
    private DateTimePicker dtPickerStart;
    private DateTimePicker dtPickerEnd;
    private String targetFileName;

    public ReceiveForm(TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        if (transformInstruction == null) throw new IllegalArgumentException("Parameter 'transformInstruction' cannot be null");
        if (currencyConverter == null) throw new IllegalArgumentException("Parameter 'currencyConverter' cannot be null");
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;

        setupUI();
    }

    private void setupUI() {
        setTitle(String.format("CryptoIso20022Interop [%s]", versionController.getVersion()));

        try {
            setIconImage(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("img/productIcon.png"))).getImage());
        } catch (IOException e) {
            ExceptionDialog.show(this, e);
        }

        var pnlMain = new JPanel();
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(pnlMain);

        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));

        var innerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        JPanel panel0 = new JPanel();
        panel0.setBorder(innerBorder);
        panel0.setLayout(new BoxLayout(panel0, BoxLayout.X_AXIS));
        JPanel panel1 = new JPanel();
        panel1.setBorder(innerBorder);
        var panel1Layout = new SpringLayout();
        panel1.setLayout(panel1Layout);
        JPanel panel2 = new JPanel();
        panel2.setBorder(innerBorder);
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
        JPanel panel3 = new JPanel();
        panel3.setBorder(innerBorder);
        var panel3Layout = new SpringLayout();
        panel3.setLayout(panel3Layout);

        pnlMain.add(panel0);
        pnlMain.add(panel1);
        pnlMain.add(panel2);
        pnlMain.add(panel3);

        panel0.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel0.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel0.setPreferredSize(new Dimension(500, 50));
        panel1.setMinimumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel1.setPreferredSize(new Dimension(500, getNorthPad(3) + 10));
        panel2.setPreferredSize(new Dimension(500, 500));
        panel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setPreferredSize(new Dimension(500, 45));

        {
            var lbl = new JLabel();
            lbl.setText("Receive Payments");
            lbl.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
            lbl.setOpaque(true);
            panel0.add(lbl);
        }

        {
            final int paddingWest = 120;
            Component anchorComponentTopLeft;
            {
                var lbl = new JLabel("Source Wallet:");
                anchorComponentTopLeft = lbl;
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(0), SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                txtInput = new WalletField(transformInstruction.getLedger().getLookupProvider());
                panel1Layout.putConstraint(SpringLayout.WEST, txtInput, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, txtInput, getNorthPad(0), SpringLayout.NORTH, panel1);
                txtInput.setLedger(transformInstruction.getLedger());
                panel1.add(txtInput);
            }
            {
                var lbl = new JLabel("Exchange rates:");
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(1), SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                var lbl2 = new JLabel(transformInstruction.getHistoricExchangeRateSource().getDisplayText());
                panel1Layout.putConstraint(SpringLayout.WEST, lbl2, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl2, getNorthPad(1), SpringLayout.NORTH, panel1);
                panel1.add(lbl2);
            }
            {
                var lbl = new JLabel("Payments between:");
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(2), SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                dtPickerStart = createDateTimePicker();
                panel1Layout.putConstraint(SpringLayout.WEST, dtPickerStart, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, dtPickerStart, getNorthPad(2), SpringLayout.NORTH, panel1);
                panel1.add(dtPickerStart);

                var lblEnd = new JLabel("and");
                panel1Layout.putConstraint(SpringLayout.WEST, lblEnd, 10, SpringLayout.EAST, dtPickerStart);
                panel1Layout.putConstraint(SpringLayout.NORTH, lblEnd, getNorthPad(2), SpringLayout.NORTH, panel1);
                lblEnd.setOpaque(true);
                panel1.add(lblEnd);

                dtPickerEnd = createDateTimePicker();
                panel1Layout.putConstraint(SpringLayout.WEST, dtPickerEnd, 10, SpringLayout.EAST, lblEnd);
                panel1Layout.putConstraint(SpringLayout.NORTH, dtPickerEnd, getNorthPad(2), SpringLayout.NORTH, panel1);
                panel1.add(dtPickerEnd);

                var cmd = new JButton("Refresh");
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> {
                    load();
                });
                panel1Layout.putConstraint(SpringLayout.EAST, cmd, 0, SpringLayout.EAST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, cmd, getNorthPad(2), SpringLayout.NORTH, panel1);
                panel1.add(cmd);
            }
        }
        {
            table = new PaymentTable(transformInstruction, currencyConverter, Actor.Receiver, new PaymentValidator());
            panel2.add(table);
        }
        {
            var cmd = new JButton("Export...");
            cmd.setPreferredSize(new Dimension(150, 35));
            cmd.addActionListener(e -> {
                exportSelected();
            });
            panel3Layout.putConstraint(SpringLayout.EAST, cmd, 0, SpringLayout.EAST, panel3);
            panel3.add(cmd);
        }
    }

    private DateTimePicker createDateTimePicker() {
        var ds = new DatePickerSettings();
        ds.setAllowEmptyDates(false);
        var df = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        var stringPattern = df.toPattern();
        ds.setFormatForDatesCommonEra(stringPattern);
        ds.setFormatForDatesBeforeCommonEra(stringPattern.replace("yyyy", "uuuu"));

        var ts = new TimePickerSettings();
        ts.setAllowEmptyTimes(false);

        return new DateTimePicker(ds, ts);
    }

    private static int getNorthPad(int line) {
        final var lineHeight = 30;
        return line * lineHeight;
    }

    private void exportSelected() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            var w = new Camt054Writer(transformInstruction.getLedger(), transformInstruction, versionController.getVersion());
            var s = CamtConverter.toXml(w.create(table.selectedPayments()));
            var outputStream = new FileOutputStream(targetFileName);
            s.writeTo(outputStream);
            outputStream.close();

            JOptionPane.showMessageDialog(table, String.format("Successfully exported to %s", targetFileName));
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void load() {
        var walletPublicKey = txtInput.getText();
        if (!transformInstruction.getLedger().isValidPublicKey(walletPublicKey)) {
            table.load(new Payment[0]);
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            var period = DateTimeRange.of(dtPickerStart.getDateTimePermissive(), dtPickerEnd.getDateTimePermissive());
            var t = new TransactionTranslator(transformInstruction, currencyConverter);
            var wallet = transformInstruction.getLedger().createWallet(walletPublicKey, null);
            var payments = t.apply(PaymentConverter.toPayment(transformInstruction.getLedger().listPaymentsReceived(wallet, period)));

            table.load(payments);
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void setWallet(Wallet wallet) {
        txtInput.setText(wallet == null ? "" : wallet.getPublicKey());
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public void setPeriod(DateTimeRange period) {
        dtPickerStart.setDateTimePermissive(period.getStart());
        dtPickerEnd.setDateTimePermissive(period.getEnd());
    }
}
