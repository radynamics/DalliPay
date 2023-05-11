package com.radynamics.dallipay.ui;

import com.alexandriasoftware.swing.JSplitButton;
import com.alexandriasoftware.swing.action.SplitButtonClickedActionListener;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.signing.NullSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.TransactionStateListener;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.transaction.TransmissionState;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.db.Database;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.iso20022.AddressFormatter;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.PaymentConverter;
import com.radynamics.dallipay.iso20022.PaymentEdit;
import com.radynamics.dallipay.iso20022.pain001.*;
import com.radynamics.dallipay.transformation.TransactionTranslator;
import com.radynamics.dallipay.transformation.TransformInstruction;
import com.radynamics.dallipay.ui.paymentTable.Actor;
import com.radynamics.dallipay.ui.paymentTable.MappingChangedListener;
import com.radynamics.dallipay.ui.paymentTable.MappingInfo;
import com.radynamics.dallipay.ui.paymentTable.PaymentTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SendForm extends JPanel implements MainFormPane, MappingChangedListener {
    private final static Logger log = LogManager.getLogger(Database.class);
    private final TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;
    private final TransactionTranslator transactionTranslator;
    private final PaymentValidator validator;
    private TransactionSubmitter submitter;

    private JLabel lblExchange;
    private JLabel lblSigningText;
    private PaymentTable table;
    private FilePathField txtInput;
    private ArrayList<Payment> payments = new ArrayList<>();
    private JSplitButton cmdAdd;
    private JButton cmdSendPayments;
    private JButton cmdExport;
    private ProgressLabel lblLoading;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public SendForm(TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        super(new GridLayout(1, 0));
        if (transformInstruction == null) throw new IllegalArgumentException("Parameter 'transformInstruction' cannot be null");
        if (currencyConverter == null) throw new IllegalArgumentException("Parameter 'currencyConverter' cannot be null");
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;
        this.transactionTranslator = new TransactionTranslator(transformInstruction, currencyConverter);
        validator = new PaymentValidator(new SenderHistoryValidator(transformInstruction.getNetwork()));

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

        final var lineCount = 3;
        final var lineHeight = 30;
        var panel1Height = lineCount * lineHeight;
        panel1.setMinimumSize(new Dimension(Integer.MAX_VALUE, panel1Height));
        panel1.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel1Height));
        panel1.setPreferredSize(new Dimension(500, panel1Height));
        panel2.setPreferredSize(new Dimension(500, 500));
        panel3.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setPreferredSize(new Dimension(500, 45));

        pnlMain.registerKeyboardAction(e -> addNewPaymentByFreeText(), KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pnlMain.registerKeyboardAction(e -> removeSelectedPayment(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        {
            final int paddingWest = 120;
            Component anchorComponentTopLeft;
            {
                var lbl = new JLabel(res.getString("input"));
                anchorComponentTopLeft = lbl;
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, 0, SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                txtInput = new FilePathField();
                try (var repo = new ConfigRepo()) {
                    txtInput.setCurrentDirectory(repo.getDefaultInputDirectory());
                } catch (Exception e) {
                    ExceptionDialog.show(this, e);
                }
                panel1Layout.putConstraint(SpringLayout.WEST, txtInput, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, txtInput, 0, SpringLayout.NORTH, panel1);
                txtInput.addChangedListener(() -> onTxtInputChanged());
                panel1.add(txtInput);
            }
            {
                var lbl = new JLabel(res.getString("exchangeRate"));
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, 30, SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                lblExchange = new JLabel();
                refreshExchange();
                panel1Layout.putConstraint(SpringLayout.WEST, lblExchange, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, lblExchange, 30, SpringLayout.NORTH, panel1);
                panel1.add(lblExchange);

                var lbl3 = Utils.createLinkLabel(this, res.getString("edit"));
                panel1Layout.putConstraint(SpringLayout.WEST, lbl3, 10, SpringLayout.EAST, lblExchange);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl3, 30, SpringLayout.NORTH, panel1);
                lbl3.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 1) {
                            showExchangeRateEdit();
                        }
                    }
                });
                panel1.add(lbl3);
            }
            {
                var lbl = new JLabel(res.getString("transmission"));
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, 60, SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                lblSigningText = new JLabel();
                panel1Layout.putConstraint(SpringLayout.WEST, lblSigningText, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, lblSigningText, 60, SpringLayout.NORTH, panel1);
                panel1.add(lblSigningText);

                var lbl3 = Utils.createLinkLabel(pnlMain, res.getString("edit"));
                panel1Layout.putConstraint(SpringLayout.WEST, lbl3, 10, SpringLayout.EAST, lblSigningText);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl3, 60, SpringLayout.NORTH, panel1);
                lbl3.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 1) {
                            for (var ledger : PaymentUtils.distinctLedgers(payments.toArray(new Payment[0]))) {
                                showSigningEdit(ledger);
                            }
                        }
                    }
                });
                panel1.add(lbl3);

                refreshSigningText();
            }
            {
                var popupMenu = new JPopupMenu();
                {
                    var item = new JMenuItem(res.getString("newManually"));
                    popupMenu.add(item);
                    item.addActionListener((SplitButtonClickedActionListener) e -> addNewEmptyPayment());
                }
                {
                    var item = new JMenuItem(res.getString("newFreeText"));
                    popupMenu.add(item);
                    item.addActionListener((SplitButtonClickedActionListener) e -> addNewPaymentByFreeText());
                }

                cmdAdd = new JSplitButton(res.getString("newAdd"));
                cmdAdd.setPopupMenu(popupMenu);
                cmdAdd.setMnemonic(KeyEvent.VK_N);
                cmdAdd.setPreferredSize(new Dimension(150, 35));
                cmdAdd.addButtonClickedActionListener(e -> addNewEmptyPayment());
                panel1Layout.putConstraint(SpringLayout.EAST, cmdAdd, 0, SpringLayout.EAST, panel1);
                panel1Layout.putConstraint(SpringLayout.SOUTH, cmdAdd, 0, SpringLayout.SOUTH, panel1);
                panel1.add(cmdAdd);
            }
        }
        {
            table = new PaymentTable(transformInstruction, currencyConverter, Actor.Sender, validator, transactionTranslator);
            table.addProgressListener(progress -> {
                lblLoading.update(progress);
                enableInputControls(progress.isFinished());
            });
            table.addPaymentListener(p -> remove(p));
            table.addMappingChangedListener(this);
            table.addSelectorChangedListener(() -> SwingUtilities.invokeLater(() -> cmdSendPayments.setEnabled(table.checkedPayments().length > 0)));
            panel2.add(table);
        }
        {
            cmdSendPayments = new JButton(res.getString("sendPayments"));
            cmdSendPayments.setMnemonic(KeyEvent.VK_S);
            cmdSendPayments.setPreferredSize(new Dimension(150, 35));
            cmdSendPayments.setEnabled(false);
            cmdSendPayments.addActionListener(e -> sendPayments());
            panel3Layout.putConstraint(SpringLayout.EAST, cmdSendPayments, 0, SpringLayout.EAST, panel3);
            panel3.add(cmdSendPayments);

            cmdExport = new JButton(res.getString("exportPending"));
            cmdExport.setMnemonic(KeyEvent.VK_E);
            cmdExport.setPreferredSize(new Dimension(150, 35));
            cmdExport.setEnabled(false);
            cmdExport.addActionListener(e -> export());
            panel3Layout.putConstraint(SpringLayout.VERTICAL_CENTER, cmdExport, 0, SpringLayout.VERTICAL_CENTER, cmdSendPayments);
            panel3Layout.putConstraint(SpringLayout.EAST, cmdExport, -170, SpringLayout.EAST, cmdSendPayments);
            panel3.add(cmdExport);

            lblLoading = new ProgressLabel();
            panel3Layout.putConstraint(SpringLayout.VERTICAL_CENTER, lblLoading, 0, SpringLayout.VERTICAL_CENTER, cmdSendPayments);
            panel3Layout.putConstraint(SpringLayout.EAST, lblLoading, -20, SpringLayout.WEST, cmdExport);
            lblLoading.setOpaque(true);
            panel3.add(lblLoading);
        }
    }

    private void removeSelectedPayment() {
        var selected = table.selectedPayments();
        if (selected.length == 0) {
            return;
        }
        remove(selected[0]);
    }

    private void remove(Payment p) {
        if (!PaymentEdit.create(p).removable()) {
            JOptionPane.showMessageDialog(this, res.getString("cannotRemove"), res.getString("removeTitle"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        var sb = new StringBuilder();
        sb.append(res.getString("removeConfirm") + System.lineSeparator());
        sb.append(p.getDisplayText());
        var ret = JOptionPane.showConfirmDialog(this, sb.toString(), res.getString("removeTitle"), JOptionPane.YES_NO_CANCEL_OPTION);
        if (ret != JOptionPane.YES_OPTION) {
            return;
        }

        payments.remove(p);
        table.remove(p);
    }

    private void addNewPaymentByFreeText() {
        var mp = ManualPayment.createByFreeText(this, transformInstruction.getLedger(), transactionTranslator);
        if (mp == null) {
            return;
        }
        showNewPayment(mp);
    }

    private void addNewEmptyPayment() {
        showNewPayment(ManualPayment.createEmpty(transformInstruction.getLedger(), transactionTranslator));
    }

    private void showNewPayment(ManualPayment mp) {
        if (!mp.show(this, validator, transformInstruction.getExchangeRateProvider(), currencyConverter)) {
            return;

        }
        var p = mp.getPayment();
        payments.add(p);
        initSubmitter();
        table.add(p);
    }

    @Override
    public void onWalletChanged(MappingInfo mi) {
        // Update all affected payments
        for (var p : payments) {
            if (mi.apply(p)) {
                switch (mi.getChangedValue()) {
                    case SenderWallet -> {
                        // Ensure a newly entered senderWallet's history is loaded for following validation calls.
                        if (mi.getMapping().isWalletPresentAndValid() && p.getSenderWallet() != null) {
                            validator.getHistoryValidator().loadHistory(p.getLedger(), p.getSenderWallet());
                        }
                    }
                    case ReceiverWallet -> {
                        // do nothing
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + mi.getChangedValue());
                }

                // Used transaction currency depends on sender/receiver wallets
                transactionTranslator.applyUserCcy(p);
            }
            table.getDataLoader().onAccountOrWalletsChanged(p);
        }
    }

    private void onTxtInputChanged() {
        PaymentInstructionReader reader;
        try {
            reader = PaymentInstructionReaderFactory.create(transformInstruction.getLedger(), new File(txtInput.getText()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ExceptionDialog.show(this, e, res.getString("readPain001Failed"));
            return;
        }

        var paramPanel = reader.createParameterPanel();
        if (paramPanel != null) {
            var frm = new ImportParameterForm(paramPanel);
            frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frm.setSize(500, 300);
            frm.setModal(true);
            frm.setLocationRelativeTo(this);
            frm.setVisible(true);

            if (!frm.isDialogAccepted() || !reader.applyParameters(paramPanel)) {
                return;
            }
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        enableInputControls(false);
        lblLoading.showLoading();

        var cf = new CompletableFuture<ArrayList<Payment>>();
        cf.thenAccept(result -> {
                    load(result);
                })
                .whenComplete((unused, e) -> {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    if (e != null) {
                        log.error(String.format("Could not read payments from %s", txtInput.getText()), e);
                        ExceptionDialog.show(this, e, res.getString("readPain001Failed"));
                    }
                });
        Executors.newCachedThreadPool().submit(() -> {
            try (var repo = new ConfigRepo()) {
                var ledger = reader.getLedger();
                transactionTranslator.setDefaultSenderWallet(ledger.getId(), repo.getDefaultSenderWallet(ledger));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            try {
                payments.clear();
                payments.addAll(List.of(transactionTranslator.apply(reader.read(new FileInputStream(txtInput.getText())))));
                transactionTranslator.applyDefaultSender(payments);
                initSubmitter();
                cf.complete(payments);
            } catch (Exception e) {
                cf.completeExceptionally(e);
            }
        });

        try (var repo = new ConfigRepo()) {
            repo.setDefaultInputDirectory(txtInput.getCurrentDirectory());
            repo.commit();
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
    }

    private void export() {
        try {
            if (txtInput.getText().length() == 0) {
                return;
            }

            if (!PaymentInstructionReaderFactory.supportsExport(new File(txtInput.getText()))) {
                JOptionPane.showMessageDialog(this, res.getString("export.notSupported"), res.getString("export.NoneSentTitle"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            var failed = new ArrayList<Payment>();
            var pain001 = Pain001Xml.read(new FileInputStream(txtInput.getText()));
            var countBefore = pain001.countCdtTrfTxInf();
            var sent = payments.stream().filter(o -> o.getTransmission() == TransmissionState.Success).collect(Collectors.toList());
            if (sent.size() == 0) {
                JOptionPane.showMessageDialog(this, res.getString("export.NoneSent"), res.getString("export.NoneSentTitle"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (var p : sent) {
                if (pain001.isRemovable(p)) {
                    pain001.remove(p);
                } else {
                    failed.add(p);
                }
            }

            var file = com.radynamics.dallipay.util.File.createWithTimeSuffix(new File(txtInput.getText()));
            pain001.writeTo(file);

            var sb = new StringBuilder();
            sb.append(String.format(res.getString("exportPaymentsSuccess"), pain001.countCdtTrfTxInf(), countBefore, file.getAbsolutePath()));
            if (failed.size() != 0) {
                sb.append(" " + res.getString("exportPaymentsIgnored") + System.lineSeparator());
                for (var f : failed) {
                    // Manually created payments may don't have a receiver address.
                    var receiverText = f.getReceiverAddress() == null ? res.getString("unknown") : AddressFormatter.formatSingleLine(f.getReceiverAddress());
                    sb.append(String.format("- %s %s%s", f.getDisplayText(), receiverText, System.lineSeparator()));
                }
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "DalliPay",
                    failed.size() == 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
    }

    private void sendPayments() {
        var payments = table.checkedPayments();
        if (!cmdSendPayments.isEnabled() || payments.length == 0) {
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            var br = new BalanceRefresher(transformInstruction.getNetwork());
            br.refreshAllSenderWallets(payments);

            var ar = new AmountRefresher(payments);
            ar.refresh(transformInstruction.getExchangeRateProvider());
            var fr = new FeeRefresher(payments);
            fr.refresh();

            // Ensure payments are still valid (ex changed exchange rates leading to not enough funds)
            Executors.newCachedThreadPool().submit(() -> {
                table.refresh(payments).thenRun(() -> sendPayments(table.checkedPayments()));
            });
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void sendPayments(Payment[] payments) {
        if (payments.length == 0) {
            return;
        }

        if (!ValidationResultDialog.showErrorAndWarnings(this, table.getValidationResults(payments))) {
            return;
        }

        var ledgers = PaymentUtils.distinctLedgers(payments);
        for (var l : ledgers) {
            sendPayments(l, payments);
        }
    }

    private void sendPayments(Ledger ledger, Payment[] payments) {
        try {
            if (submitter == null && !showSigningEdit(ledger)) {
                return;
            }

            try (var repo = new ConfigRepo()) {
                repo.setLastUsedSubmitter(submitter);
                repo.commit();
            } catch (Exception e) {
                ExceptionDialog.show(this, e);
            }

            if (!showConfirmationForm(ledger, payments)) {
                return;
            }

            var results = validator.validate(payments);
            if (!ValidationResultDialog.showErrorAndWarnings(this, results)) {
                return;
            }

            submitter.addStateListener(new TransactionStateListener() {
                @Override
                public void onProgressChanged(Transaction t) {
                    table.refresh(getPayment(t));
                }

                @Override
                public void onSuccess(Transaction t) {
                    table.refresh(getPayment(t));
                }

                @Override
                public void onFailure(Transaction t) {
                    table.refresh(getPayment(t));
                }
            });
            var privateKeyProvider = submitter.getPrivateKeyProvider();
            if (!privateKeyProvider.collect(payments)) {
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            submitter.submit(PaymentConverter.toTransactions(payments));
        } catch (Exception ex) {
            ExceptionDialog.show(this, ex);
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void initSubmitter() {
        if (submitter != null || payments.size() == 0) {
            return;
        }
        setSubmitter(getLastUsedSubmitter(payments.get(0).getLedger()));
    }

    private TransactionSubmitter getLastUsedSubmitter(Ledger ledger) {
        try (var repo = new ConfigRepo()) {
            return repo.getLastUsedSubmitter(this, ledger);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private Payment getPayment(Transaction t) {
        for (var p : payments) {
            if (p.is(t)) {
                return p;
            }
        }
        throw new RuntimeException("Could not find matching payment for transaction.");
    }

    public void refreshDefaultSenderWallet(LedgerId ledgerId, Wallet wallet) {
        transactionTranslator.setDefaultSenderWallet(ledgerId, wallet);
    }

    private void refreshExchange() {
        lblExchange.setText(transformInstruction.getExchangeRateProvider().getDisplayText());
    }

    private void showExchangeRateEdit() {
        var uniques = new HashMap<String, ExchangeRate>();
        var editablePayments = new HashSet<Payment>();
        for (var p : payments) {
            if (p.isSameCcy() || !PaymentEdit.create(p).exchangeRateEditable()) {
                continue;
            }
            var r = p.getExchangeRate();
            if (r == null) {
                r = ExchangeRate.Undefined(p.createCcyPair());
            }
            uniques.put(r.getPair().getDisplayText(), r);
            editablePayments.add(p);
        }

        if (uniques.size() == 0) {
            JOptionPane.showMessageDialog(this, res.getString("exchangeRateNoPayments"), res.getString("exchangeRateNoPaymentsTitle"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        var frm = new ExchangeRatesForm(transformInstruction.getLedger(), transformInstruction.getExchangeRateProvider(), uniques.values().toArray(new ExchangeRate[0]), ZonedDateTime.now());
        frm.setAllowChangeExchange(true);
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(500, 300);
        frm.setModal(true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);

        if (!frm.isDialogAccepted()) {
            return;
        }

        transformInstruction.setExchangeRateProvider(frm.getSelectedExchange());
        try (var repo = new ConfigRepo()) {
            repo.setExchangeRateProvider(transformInstruction.getExchangeRateProvider());
            repo.commit();
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
        refreshExchange();
        for (var ccyPairItem : uniques.entrySet()) {
            for (var p : editablePayments) {
                if (ccyPairItem.getKey().equals(p.createCcyPair().getDisplayText())) {
                    p.setExchangeRate(ccyPairItem.getValue());
                }
            }
        }

        var paymentArray = editablePayments.toArray(new Payment[0]);
        var ar = new AmountRefresher(paymentArray);
        ar.refresh();

        table.refresh(paymentArray);
    }

    private boolean showSigningEdit(Ledger ledger) {
        var submitter = SubmitterSelectionForm.showDialog(this, ledger, this.submitter);
        if (submitter == null) {
            return false;
        }
        setSubmitter(submitter);
        return true;
    }

    private void setSubmitter(TransactionSubmitter submitter) {
        this.submitter = submitter;
        refreshSigningText();

        for (var p : payments) {
            // Prevent setting null to payments
            p.setSubmitter(this.submitter == null ? new NullSubmitter() : this.submitter);
            // Eg. pathfinding is supported only by specific signers.
            // TODO: refresh paymnets (p.refreshPaymentPath(currencyConverter))
        }
    }

    private void refreshSigningText() {
        lblSigningText.setText(String.format(res.getString("signUsing"), submitter == null ? res.getString("unknown") : submitter.getInfo().getTitle()));
    }

    private boolean showConfirmationForm(Ledger ledger, Payment[] payments) {
        var frm = new SendConfirmationForm(ledger, payments, transformInstruction.getExchangeRateProvider(), this.payments.size());
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(600, 410);
        frm.setModal(true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
        return frm.isDialogAccepted();
    }

    private void load(ArrayList<Payment> payments) {
        if (payments == null) throw new IllegalArgumentException("Parameter 'payments' cannot be null");
        this.payments = payments;
        loadTable(payments);
    }

    private void enableInputControls(boolean enabled) {
        txtInput.setEnabled(enabled);
        table.setEditable(enabled);
        cmdAdd.setEnabled(enabled);
        cmdExport.setEnabled(enabled);
        cmdSendPayments.setEnabled(enabled);
    }

    private void loadTable(ArrayList<Payment> payments) {
        table.load(payments.toArray(new Payment[0]));
        cmdSendPayments.setEnabled(false);
    }

    public void setInput(String value) {
        txtInput.setText(value);
    }

    @Override
    public String getTitle() {
        return res.getString("title");
    }

    public void setNetwork(NetworkInfo networkInfo) {
        validator.getHistoryValidator().setNetwork(networkInfo);

        for (var p : payments) {
            p.getLedger().setNetwork(networkInfo);
        }
        load(payments);
    }
}
