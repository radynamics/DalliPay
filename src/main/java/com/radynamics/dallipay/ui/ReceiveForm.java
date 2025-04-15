package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.VersionController;
import com.radynamics.dallipay.cryptoledger.TransactionResult;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.xrpl.XrplPriceOracleConfig;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.exchange.*;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.PaymentConverter;
import com.radynamics.dallipay.iso20022.camt054.*;
import com.radynamics.dallipay.paymentrequest.ReceiveRequest;
import com.radynamics.dallipay.transformation.AccountMappingSource;
import com.radynamics.dallipay.transformation.AccountMappingSourceException;
import com.radynamics.dallipay.transformation.TransactionTranslator;
import com.radynamics.dallipay.transformation.TransformInstruction;
import com.radynamics.dallipay.ui.paymentTable.Actor;
import com.radynamics.dallipay.ui.paymentTable.PaymentTable;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class ReceiveForm extends JPanel implements MainFormPane {
    private TransformInstruction transformInstruction;
    private TransactionTranslator transactionTranslator;
    private final VersionController versionController = new VersionController();

    private PaymentTable table;
    private WalletField txtInput;
    private DateTimePicker dtPickerStart;
    private DateTimePicker dtPickerEnd;
    private String targetFileName;
    private CamtFormat defaultExportFormat;
    private CamtExport camtExport;
    private JButton cmdRefresh;
    private JButton cmdExport;
    private ProgressLabel lblLoading;
    private JComboBox<StringPairEntry> cboTargetCcy;
    private JPanel pnlInfo;
    private JLabel lblInfoText;
    private final JLabel lblUsingExchangeRatesFrom = new JLabel();
    private final JLabel lblUsingExchangeRatesFromSource = new JLabel();
    private JLabel lblHistoricPriceOracleEdit;
    private final ArrayList<ReceiveListener> listener = new ArrayList<>();
    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public ReceiveForm() {
        super(new GridLayout(1, 0));

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

        var panel1Height = getNorthPad(3);
        panel1.setMinimumSize(new Dimension(Integer.MAX_VALUE, panel1Height));
        panel1.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel1Height));
        panel1.setPreferredSize(new Dimension(500, panel1Height));
        panel2.setPreferredSize(new Dimension(500, 500));
        panel3.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setPreferredSize(new Dimension(500, 45));

        pnlMain.registerKeyboardAction(e -> {
            if (isEnabled()) {
                load();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        {
            final int paddingWest = 130;
            Component anchorComponentTopLeft;
            {
                var lbl = new JLabel(res.getString("receiverWallet"));
                anchorComponentTopLeft = lbl;
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(0), SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                txtInput = new WalletField(this);
                txtInput.allowGeneralTerm(true);
                panel1Layout.putConstraint(SpringLayout.WEST, txtInput, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, txtInput, getNorthPad(0), SpringLayout.NORTH, panel1);
                panel1.add(txtInput);
            }
            {
                var lbl = new JLabel(res.getString("targetCcy"));
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(1), SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                cboTargetCcy = new JComboBox<>();
                cboTargetCcy.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        return super.getListCellRendererComponent(list, ((StringPairEntry) value).getDisplayText(), index, isSelected, cellHasFocus);
                    }
                });
                cboTargetCcy.addItemListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        onSelectedTargetCcyChanged(((StringPairEntry) e.getItem()).key);
                    }
                });
                panel1Layout.putConstraint(SpringLayout.WEST, cboTargetCcy, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, cboTargetCcy, getNorthPad(1), SpringLayout.NORTH, panel1);
                panel1.add(cboTargetCcy);

                lblUsingExchangeRatesFrom.setText(" " + res.getString("usingFxRatesFrom") + " ");
                panel1Layout.putConstraint(SpringLayout.WEST, lblUsingExchangeRatesFrom, 0, SpringLayout.EAST, cboTargetCcy);
                panel1Layout.putConstraint(SpringLayout.NORTH, lblUsingExchangeRatesFrom, getNorthPad(1), SpringLayout.NORTH, panel1);
                panel1.add(lblUsingExchangeRatesFrom);

                panel1Layout.putConstraint(SpringLayout.WEST, lblUsingExchangeRatesFromSource, 0, SpringLayout.EAST, lblUsingExchangeRatesFrom);
                panel1Layout.putConstraint(SpringLayout.NORTH, lblUsingExchangeRatesFromSource, getNorthPad(1), SpringLayout.NORTH, panel1);
                panel1.add(lblUsingExchangeRatesFromSource);

                lblHistoricPriceOracleEdit = Utils.createLinkLabel(this, res.getString("edit"));
                panel1Layout.putConstraint(SpringLayout.WEST, lblHistoricPriceOracleEdit, 5, SpringLayout.EAST, lblUsingExchangeRatesFromSource);
                panel1Layout.putConstraint(SpringLayout.NORTH, lblHistoricPriceOracleEdit, getNorthPad(1), SpringLayout.NORTH, panel1);
                panel1.add(lblHistoricPriceOracleEdit);
                lblHistoricPriceOracleEdit.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 1) {
                            editHistoricPriceOracle();
                        }
                    }
                });
            }
            {
                var lbl = new JLabel(res.getString("paymentsBetween"));
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(2), SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                dtPickerStart = createDateTimePicker();
                panel1Layout.putConstraint(SpringLayout.WEST, dtPickerStart, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, dtPickerStart, getNorthPad(2), SpringLayout.NORTH, panel1);
                panel1.add(dtPickerStart);

                var lblEnd = new JLabel(res.getString("and"));
                panel1Layout.putConstraint(SpringLayout.WEST, lblEnd, 10, SpringLayout.EAST, dtPickerStart);
                panel1Layout.putConstraint(SpringLayout.NORTH, lblEnd, getNorthPad(2), SpringLayout.NORTH, panel1);
                lblEnd.setOpaque(true);
                panel1.add(lblEnd);

                dtPickerEnd = createDateTimePicker();
                panel1Layout.putConstraint(SpringLayout.WEST, dtPickerEnd, 10, SpringLayout.EAST, lblEnd);
                panel1Layout.putConstraint(SpringLayout.NORTH, dtPickerEnd, getNorthPad(2), SpringLayout.NORTH, panel1);
                panel1.add(dtPickerEnd);

                cmdRefresh = new JButton(res.getString("refresh"));
                cmdRefresh.setMnemonic(KeyEvent.VK_R);
                cmdRefresh.setPreferredSize(new Dimension(150, 35));
                cmdRefresh.addActionListener(e -> {
                    load();
                });
                panel1Layout.putConstraint(SpringLayout.EAST, cmdRefresh, 0, SpringLayout.EAST, panel1);
                panel1Layout.putConstraint(SpringLayout.SOUTH, cmdRefresh, 0, SpringLayout.SOUTH, panel1);
                panel1.add(cmdRefresh);
            }
        }
        {
            table = new PaymentTable(Actor.Receiver);
            table.addProgressListener(progress -> {
                lblLoading.update(progress);
                if (progress.isFinished()) {
                    raiseOnReceiveCompleted();
                }
                enableInputControls(isEnabled() && progress.isFinished());
            });
            table.addSelectorChangedListener(() -> cmdExport.setEnabled(table.checkedPayments().length > 0));
            table.setEmptyBackgroundText(res.getString("noPayments"));
            panel2.add(table);
        }
        {
            cmdExport = new JButton(res.getString("export"));
            cmdExport.setMnemonic(KeyEvent.VK_E);
            cmdExport.setPreferredSize(new Dimension(150, 35));
            cmdExport.setEnabled(false);
            cmdExport.addActionListener(e -> {
                exportChecked();
            });
            panel3Layout.putConstraint(SpringLayout.EAST, cmdExport, 0, SpringLayout.EAST, panel3);
            panel3.add(cmdExport);

            {
                pnlInfo = new JPanel();
                pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.X_AXIS));
                panel3Layout.putConstraint(SpringLayout.VERTICAL_CENTER, pnlInfo, 0, SpringLayout.VERTICAL_CENTER, cmdExport);
                panel3Layout.putConstraint(SpringLayout.WEST, pnlInfo, 0, SpringLayout.WEST, table);
                panel3.add(pnlInfo);
                {
                    var lbl = new JLabel();
                    lbl.setIcon(new FlatSVGIcon("svg/informationDialog.svg", 16, 16));
                    pnlInfo.add(lbl);
                }
                pnlInfo.add(Box.createRigidArea(new Dimension(5, 0)));
                {
                    lblInfoText = new JLabel();
                    lblInfoText.setOpaque(true);
                    pnlInfo.add(lblInfoText);
                }
                hideInfo();
            }

            lblLoading = new ProgressLabel();
            panel3Layout.putConstraint(SpringLayout.VERTICAL_CENTER, lblLoading, 0, SpringLayout.VERTICAL_CENTER, cmdExport);
            panel3Layout.putConstraint(SpringLayout.EAST, lblLoading, -20, SpringLayout.WEST, cmdExport);
            lblLoading.setOpaque(true);
            panel3.add(lblLoading);
        }
    }

    private void editHistoricPriceOracle() {
        var allProvider = ExchangeRateProviderFactory.allPriceOracles(transformInstruction.getLedger());
        try (var repo = new ConfigRepo()) {
            for (var p : allProvider) {
                p.init(repo);
            }
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
            return;
        }

        var pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));

        pnl.add(new JLabel(res.getString("whichHistoricPriceOracle")));
        pnl.add(Box.createRigidArea(new Dimension(0, 5)));
        var group = new ButtonGroup();
        var map = new HashMap<JRadioButton, ExchangeRateProvider>();
        for (var p : allProvider) {
            var rdo = new JRadioButton(p.getDisplayText());
            map.put(rdo, p);
            rdo.setSelected(p.getId().equals(transformInstruction.getHistoricExchangeRateSource().getId()));
            group.add(rdo);
            pnl.add(rdo);
        }

        var result = JOptionPane.showOptionDialog(this, pnl, res.getString("historicPriceOracleTitle"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"OK", res.getString("cancel")}, "OK");
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        var selected = map.entrySet().stream().filter(o -> o.getKey().isSelected()).findFirst().orElseThrow().getValue();
        transformInstruction.setHistoricExchangeRateSource(selected);
        try (var repo = new ConfigRepo()) {
            repo.setHistoricExchangeRateSource(transformInstruction.getLedger(), transformInstruction.getHistoricExchangeRateSource());
            repo.commit();
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
        refreshUsedPriceOracleText();
    }

    private void hideInfo() {
        pnlInfo.setVisible(false);
    }

    private void showInfo(String text) {
        lblInfoText.setText(text);
        pnlInfo.setVisible(true);
    }

    private void onSelectedTargetCcyChanged(String ccy) {
        try (var repo = new ConfigRepo()) {
            repo.setTargetCcy(ccy);
            repo.commit();
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }

        var isAsReceived = ccy.equals(XrplPriceOracleConfig.AsReceived);
        lblUsingExchangeRatesFrom.setVisible(!isAsReceived);
        lblUsingExchangeRatesFromSource.setVisible(!isAsReceived);
        lblHistoricPriceOracleEdit.setVisible(lblUsingExchangeRatesFromSource.isVisible());

        transformInstruction.setTargetCcy(ccy);
    }

    private void refreshTargetCcys() {
        String selectedCcy = null;
        var historicExchangeRateSource = transformInstruction.getHistoricExchangeRateSource();
        try (var repo = new ConfigRepo()) {
            selectedCcy = repo.getTargetCcy(transformInstruction.getTargetCcy());
            historicExchangeRateSource.init(repo);
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }

        refreshTargetCcys(selectedCcy, historicExchangeRateSource);
    }

    public void refreshTargetCcys(String selectedCcy, ExchangeRateProvider historicExchangeRateSource) {
        var ccys = new ArrayList<StringPairEntry>();
        for (var pair : historicExchangeRateSource.getSupportedPairs()) {
            var ccy = pair.getSecondCode();
            ccys.add(new StringPairEntry(ccy, ccy));
        }

        cboTargetCcy.removeAllItems();
        ccys.sort(Comparator.comparing(o -> o.displayText));
        cboTargetCcy.addItem(new StringPairEntry(XrplPriceOracleConfig.AsReceived, XrplPriceOracleConfig.AsReceivedText));
        for (var ccy : ccys) {
            cboTargetCcy.addItem(ccy);
            if (ccy.getKey().equalsIgnoreCase(selectedCcy)) {
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

    private void exportChecked() {
        try {
            var xml = createCamtOfChecked();
            if (xml == null) {
                return;
            }
            var outputStream = new FileOutputStream(targetFileName);
            xml.writeTo(outputStream);
            outputStream.close();

            JOptionPane.showMessageDialog(table, String.format(res.getString("exportSuccess"), targetFileName));
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
    }

    public ByteArrayOutputStream createCamtOfChecked() {
        if (lblLoading.isLoading() || table.checkedPayments().length == 0) {
            return null;
        }

        if (!showExportForm()) {
            return null;
        }

        AccountMappingSource accountMappingSource = null;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            var w = camtExport.getWriter();
            accountMappingSource = w.getTransformInstruction().getAccountMappingSource();
            try (var repo = new ConfigRepo()) {
                w.getTransformInstruction().setBookingDateFormat(repo.getBookingDateFormat());
                w.getTransformInstruction().setValutaDateFormat(repo.getValutaDateFormat());
                w.getTransformInstruction().setCreditorReferenceIfMissing(repo.getCreditorReferenceIfMissing());
            }
            var camtConverter = camtExport.getConverter();
            accountMappingSource.open();
            return camtConverter.toXml(w.createDocument(table.checkedPayments()));
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
            return null;
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            try {
                accountMappingSource.close();
            } catch (AccountMappingSourceException e) {
                ExceptionDialog.show(this, e);
            }
        }
    }

    private boolean showExportForm() {
        if (StringUtils.isAllEmpty(targetFileName)) {
            targetFileName = createTargetFile().getAbsolutePath();
        }
        var exportFormat = defaultExportFormat == null ? CamtFormatHelper.getDefault() : defaultExportFormat;
        var exportLedgerCurrencyFormat = transformInstruction.getLedger().createLedgerCurrencyConverter(LedgerCurrencyFormat.Native).getDefaultTargetFormat();
        if (camtExport == null) {
            try (var repo = new ConfigRepo()) {
                exportFormat = repo.getDefaultExportFormat();
                exportLedgerCurrencyFormat = repo.getExportLedgerCurrencyFormat(transformInstruction.getLedger());
            } catch (Exception e) {
                ExceptionDialog.show(this, e);
            }
        } else {
            exportFormat = camtExport.getWriter().getExportFormat();
            exportLedgerCurrencyFormat = camtExport.getWriter().getExportLedgerCurrencyFormat();
        }

        var frm = new ReceiveExportForm(transformInstruction.getLedger());
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(600, 300);
        frm.setModal(true);
        frm.setLocationRelativeTo(this);
        frm.setOutputFile(targetFileName);
        frm.setExportFormat(exportFormat);
        frm.setLedgerCurrencyFormat(exportLedgerCurrencyFormat);
        frm.setVisible(true);
        if (!frm.isDialogAccepted()) {
            return false;
        }
        if (frm.getOutputFile().length() == 0) {
            JOptionPane.showMessageDialog(this, res.getString("exportEnterPath"));
            return false;
        }

        targetFileName = frm.getOutputFile();
        try (var repo = new ConfigRepo()) {
            repo.setDefaultOutputDirectory(new File(targetFileName).getParentFile());
            repo.setDefaultExportFormat(frm.getExportFormat());
            repo.setExportLedgerCurrencyFormat(transformInstruction.getLedger().getId(), frm.getExportLedgerCurrencyFormat());
            repo.commit();
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
        camtExport = CamtExportFactory.create(frm.getExportFormat(), frm.getExportLedgerCurrencyFormat(), transformInstruction, versionController);
        return true;
    }

    private File createTargetFile() {
        var dir = new File(System.getProperty("user.dir"));
        try (var repo = new ConfigRepo()) {
            var defaultDir = repo.getDefaultOutputDirectory();
            dir = defaultDir == null ? dir : defaultDir;
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }

        var df = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss");
        var fileName = String.format("%s_%s.xml", ZonedDateTime.now().format(df), txtInput.getText());
        return new File(dir, fileName);
    }

    public void load() {
        var walletInput = txtInput.getWalletInput();
        if (!walletInput.valid()) {
            loadTable(new Payment[0]);
            return;
        }

        if (lblLoading.isLoading()) {
            return;
        }

        var selectedTargetCcy = ((StringPairEntry) cboTargetCcy.getSelectedItem()).getKey();
        var targetCcy = selectedTargetCcy.equals(XrplPriceOracleConfig.AsReceived) ? null : new Currency(selectedTargetCcy);
        if (targetCcy != null) {
            var ccyPair = new CurrencyPair(transformInstruction.getLedger().getNativeCcySymbol(), targetCcy.getCode());
            var supportedPairs = transformInstruction.getHistoricExchangeRateSource().getSupportedPairs();
            if (!ccyPair.isOneToOne() && !CurrencyPair.contains(supportedPairs, ccyPair)) {
                JOptionPane.showMessageDialog(this,
                        String.format(res.getString("doesNotSupportFxRate"), transformInstruction.getHistoricExchangeRateSource().getDisplayText(), ccyPair.getDisplayText()),
                        res.getString("doesNotSupportFxRateTitle"), JOptionPane.INFORMATION_MESSAGE);
            }
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        enableInputControls(false);
        lblLoading.showLoading();
        var period = DateTimeRange.of(dtPickerStart.getDateTimePermissive(), dtPickerEnd.getDateTimePermissive());
        transactionTranslator.setTargetCcy(targetCcy);

        var cf = new CompletableFuture<TransactionResult>();
        cf.thenAccept(result -> {
                    var payments = transactionTranslator.apply(PaymentConverter.toPayment(result.transactions(), targetCcy));
                    loadTable(payments);

                    if (result.hasMarker()) {
                        showInfo(res.getString("hasMarker"));
                    } else if (result.hasMaxPageCounterReached()) {
                        showInfo(res.getString("maxPageCounter"));
                    } else if (result.hasNoTransactions()) {
                        showInfo(result.existsWallet() ? res.getString("noTrx") : res.getString("walletDoesntExist"));
                    } else {
                        hideInfo();
                    }
                })
                .whenComplete((unused, e) -> {
                    if (!table.getDataLoader().isLoading()) {
                        lblLoading.hideLoading();
                    }
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    if (e != null) {
                        enableInputControls(isEnabled());
                        ExceptionDialog.show(this, e);
                    }
                });
        Executors.newCachedThreadPool().submit(() -> {
            try {
                cf.complete(transformInstruction.getLedger().listPaymentsReceived(walletInput, period));
            } catch (Exception e) {
                cf.completeExceptionally(e);
            }
        });
    }

    private void enableInputControls(boolean enabled) {
        cmdRefresh.setEnabled(enabled);
        table.setEditable(enabled);
        cmdExport.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        enableInputControls(enabled);
        txtInput.setEditable(enabled);
    }

    private void loadTable(Payment[] payments) {
        table.load(payments);
        cmdExport.setEnabled(false);
    }

    public void setWallet(Wallet wallet) {
        txtInput.setText(wallet == null ? "" : wallet.getPublicKey());
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public void setPeriod(DateTimeRange period) {
        dtPickerStart.setDateTimePermissive(period.getStart().toLocalDateTime());
        dtPickerEnd.setDateTimePermissive(period.getEnd().toLocalDateTime());
    }

    @Override
    public String getTitle() {
        return res.getString("title");
    }

    public void init(TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        if (transformInstruction == null) throw new IllegalArgumentException("Parameter 'transformInstruction' cannot be null");
        if (currencyConverter == null) throw new IllegalArgumentException("Parameter 'currencyConverter' cannot be null");
        this.transformInstruction = transformInstruction;
        this.transactionTranslator = new TransactionTranslator(transformInstruction, currencyConverter);

        txtInput.setLedger(transformInstruction.getLedger());
        refreshTargetCcys();
        refreshUsedPriceOracleText();
        table.init(transformInstruction, currencyConverter, new PaymentValidator(), transactionTranslator);
        // Clear loaded payments
        loadTable(new Payment[0]);
    }

    private void refreshUsedPriceOracleText() {
        lblUsingExchangeRatesFromSource.setText(transformInstruction.getHistoricExchangeRateSource().getDisplayText());
    }

    public void changeFilter(ReceiveRequest args) {
        if (!StringUtils.isEmpty(args.wallet())) {
            setWallet(transformInstruction.getLedger().createWallet(args.wallet(), null));
        }
        setPeriod(DateTimeRange.of(args.from(), args.to()));
        // TODO: AccountMapping
        if (!StringUtils.isEmpty(args.format())) {
            if ("camt053".equals(args.format())) {
                defaultExportFormat = CamtFormat.Camt05300108;
            } else if ("camt054".equals(args.format())) {
                defaultExportFormat = CamtFormat.Camt05400109;
            }
        }
    }

    public void addReceiveListener(ReceiveListener l) {
        listener.add(l);
    }

    public void removeReceiveListener(ReceiveListener l) {
        listener.remove(l);
    }

    private void raiseOnReceiveCompleted() {
        // Do not use for as the listener calls removeReceiveListener withing onReceiveCompleted.
        for (var i = listener.size() - 1; i >= 0; i--) {
            listener.get(i).onReceiveCompleted();
        }
    }

    private class StringPairEntry {
        private final String key;
        private final String displayText;

        public StringPairEntry(String key, String displayText) {
            this.key = key;
            this.displayText = displayText;
        }

        public String getKey() {
            return key;
        }

        public String getDisplayText() {
            return displayText;
        }

        @Override
        public String toString() {
            return String.format("%s, %s", key, displayText);
        }
    }
}
