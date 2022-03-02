package com.radynamics.CryptoIso20022Interop.ui;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.VersionController;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet;
import com.radynamics.CryptoIso20022Interop.db.ConfigRepo;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.PaymentConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.CamtExport;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.CamtExportFactory;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.CamtFormat;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.PaymentValidator;
import com.radynamics.CryptoIso20022Interop.transformation.TransactionTranslator;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.ui.paymentTable.Actor;
import com.radynamics.CryptoIso20022Interop.ui.paymentTable.PaymentTable;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiveForm extends JPanel implements MainFormPane {
    private TransformInstruction transformInstruction;
    private CurrencyConverter currencyConverter;
    private final VersionController versionController = new VersionController();

    private PaymentTable table;
    private WalletField txtInput;
    private DateTimePicker dtPickerStart;
    private DateTimePicker dtPickerEnd;
    private String targetFileName;
    private CamtExport camtExport;
    private JButton cmdExport;
    private JLabel lblLoading;
    private JComboBox<String> cboTargetCcy;

    public ReceiveForm(TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        super(new GridLayout(1, 0));
        if (transformInstruction == null) throw new IllegalArgumentException("Parameter 'transformInstruction' cannot be null");
        if (currencyConverter == null) throw new IllegalArgumentException("Parameter 'currencyConverter' cannot be null");
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;
        this.camtExport = CamtExportFactory.create(CamtFormat.Camt05400104, this.transformInstruction, versionController);

        setupUI();
    }

    private void setupUI() {
        var pnlMain = new JPanel();
        add(pnlMain);

        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));

        var innerBorder = BorderFactory.createEmptyBorder(5, 0, 5, 0);
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

        pnlMain.add(panel1);
        pnlMain.add(panel2);
        pnlMain.add(panel3);

        panel1.setMinimumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel1.setPreferredSize(new Dimension(500, getNorthPad(3) + 20));
        panel2.setPreferredSize(new Dimension(500, 500));
        panel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setPreferredSize(new Dimension(500, 45));

        {
            final int paddingWest = 120;
            Component anchorComponentTopLeft;
            {
                var lbl = new JLabel("Receiver Wallet:");
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
                var lbl = new JLabel("Target currency:");
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(1), SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                cboTargetCcy = new JComboBox<>();
                cboTargetCcy.addItemListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        onSelectedTargetCcyChanged((String) e.getItem());
                    }
                });
                fillTargetCcy();
                panel1Layout.putConstraint(SpringLayout.WEST, cboTargetCcy, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, cboTargetCcy, getNorthPad(1), SpringLayout.NORTH, panel1);
                panel1.add(cboTargetCcy);

                var lbl3 = new JLabel(" using exchange rates from ");
                panel1Layout.putConstraint(SpringLayout.WEST, lbl3, 0, SpringLayout.EAST, cboTargetCcy);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl3, getNorthPad(1), SpringLayout.NORTH, panel1);
                panel1.add(lbl3);

                var lbl2 = new JLabel(transformInstruction.getHistoricExchangeRateSource().getDisplayText());
                panel1Layout.putConstraint(SpringLayout.WEST, lbl2, 0, SpringLayout.EAST, lbl3);
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
            table.addProgressListener(progress -> {
                if (progress.getCount() == progress.getTotal()) {
                    lblLoading.setText("");
                    cmdExport.setEnabled(true);
                } else {
                    lblLoading.setText(String.format("Loaded %s / %s...", progress.getCount(), progress.getTotal()));
                }
            });
            panel2.add(table);
        }
        {
            cmdExport = new JButton("Export...");
            cmdExport.setPreferredSize(new Dimension(150, 35));
            cmdExport.addActionListener(e -> {
                exportSelected();
            });
            panel3Layout.putConstraint(SpringLayout.EAST, cmdExport, 0, SpringLayout.EAST, panel3);
            panel3.add(cmdExport);

            lblLoading = new JLabel();
            panel3Layout.putConstraint(SpringLayout.VERTICAL_CENTER, lblLoading, 0, SpringLayout.VERTICAL_CENTER, cmdExport);
            panel3Layout.putConstraint(SpringLayout.EAST, lblLoading, -20, SpringLayout.WEST, cmdExport);
            lblLoading.setOpaque(true);
            panel3.add(lblLoading);
        }
    }

    private void onSelectedTargetCcyChanged(String ccy) {
        try (var repo = new ConfigRepo()) {
            repo.setTargetCcy(ccy);
            repo.getConnection().commit();
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }

        transformInstruction.setTargetCcy(ccy);
    }

    private void fillTargetCcy() {
        String selectedCcy = null;
        try (var repo = new ConfigRepo()) {
            selectedCcy = repo.getTargetCcy(transformInstruction.getTargetCcy());
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }

        var ccys = new String[]{"USD", "EUR", "JPY", "XRP"};
        for (var ccy : ccys) {
            cboTargetCcy.addItem(ccy);
            if (ccy.equalsIgnoreCase(selectedCcy)) {
                cboTargetCcy.setSelectedItem(ccy);
            }
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
            if (!showExportForm()) {
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            var w = camtExport.getWriter();
            try (var repo = new ConfigRepo()) {
                w.getTransformInstruction().setBookingDateFormat(repo.getBookingDateFormat());
                w.getTransformInstruction().setValutaDateFormat(repo.getValutaDateFormat());
            }
            var camtConverter = camtExport.getConverter();
            var s = camtConverter.toXml(w.createDocument(table.selectedPayments()));
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

    private boolean showExportForm() {
        if (StringUtils.isAllEmpty(targetFileName)) {
            var df = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss");
            targetFileName = String.format("%s_%s.xml", LocalDateTime.now().format(df), txtInput.getText());
        }

        var frm = new ReceiveExportForm();
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(600, 300);
        frm.setModal(true);
        frm.setLocationRelativeTo(this);
        frm.setOutputFile(targetFileName);
        frm.setVisible(true);
        if (!frm.isDialogAccepted()) {
            return false;
        }
        if (frm.getOutputFile().length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter an export file path to export received payments.");
            return false;
        }

        targetFileName = frm.getOutputFile();
        camtExport = CamtExportFactory.create(frm.getExportFormat(), transformInstruction, versionController);
        return true;
    }

    public void load() {
        var walletPublicKey = txtInput.getText();
        if (!transformInstruction.getLedger().isValidPublicKey(walletPublicKey)) {
            table.load(new Payment[0]);
            return;
        }

        var targetCcy = cboTargetCcy.getSelectedItem().toString();
        var ccyPair = new CurrencyPair(transformInstruction.getLedger().getNativeCcySymbol(), targetCcy);
        var supportedPairs = transformInstruction.getHistoricExchangeRateSource().getSupportedPairs();
        if (!ccyPair.isOneToOne() && !CurrencyPair.contains(supportedPairs, ccyPair)) {
            JOptionPane.showMessageDialog(this,
                    String.format("%s does not support exchange rates for %s", transformInstruction.getHistoricExchangeRateSource().getDisplayText(), ccyPair.getDisplayText()),
                    "Currency not supported", JOptionPane.INFORMATION_MESSAGE);
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            cmdExport.setEnabled(false);
            lblLoading.setText("Loading, please wait...");
            var period = DateTimeRange.of(dtPickerStart.getDateTimePermissive(), dtPickerEnd.getDateTimePermissive());
            var t = new TransactionTranslator(transformInstruction, currencyConverter);
            t.setTargetCcy(targetCcy);
            var wallet = transformInstruction.getLedger().createWallet(walletPublicKey, null);
            var payments = t.apply(PaymentConverter.toPayment(transformInstruction.getLedger().listPaymentsReceived(wallet, period), targetCcy));

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

    @Override
    public String getTitle() {
        return "Receive Payments";
    }
}
