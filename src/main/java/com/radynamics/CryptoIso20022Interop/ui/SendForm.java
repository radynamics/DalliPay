package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.db.ConfigRepo;
import com.radynamics.CryptoIso20022Interop.db.Database;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.PaymentConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.Pain001Reader;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.PaymentValidator;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.SenderHistoryValidator;
import com.radynamics.CryptoIso20022Interop.transformation.TransactionTranslator;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.ui.paymentTable.Actor;
import com.radynamics.CryptoIso20022Interop.ui.paymentTable.PaymentTable;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class SendForm extends JPanel implements MainFormPane {
    private final static Logger log = LogManager.getLogger(Database.class);
    private final Window owner;
    private TransformInstruction transformInstruction;
    private CurrencyConverter currencyConverter;
    private final PaymentValidator validator = new PaymentValidator(new SenderHistoryValidator());

    private JLabel lblExchange;
    private PaymentTable table;
    private FilePathField txtInput;
    private Pain001Reader reader;
    private Payment[] payments = new Payment[0];
    private JButton cmdSendPayments;
    private ProgressLabel lblLoading;

    public SendForm(Window owner, TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        super(new GridLayout(1, 0));
        if (owner == null) throw new IllegalArgumentException("Parameter 'owner' cannot be null");
        if (transformInstruction == null) throw new IllegalArgumentException("Parameter 'transformInstruction' cannot be null");
        if (currencyConverter == null) throw new IllegalArgumentException("Parameter 'currencyConverter' cannot be null");
        this.owner = owner;
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;

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
        panel3.setLayout(panel3Layout/*new FlowLayout(FlowLayout.RIGHT)*/);

        pnlMain.add(panel1);
        pnlMain.add(panel2);
        pnlMain.add(panel3);

        panel1.setMinimumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel1.setPreferredSize(new Dimension(500, 70));
        panel2.setPreferredSize(new Dimension(500, 500));
        panel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setPreferredSize(new Dimension(500, 45));

        {
            final int paddingWest = 120;
            Component anchorComponentTopLeft;
            {
                var lbl = new JLabel("Input:");
                anchorComponentTopLeft = lbl;
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, 0, SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                txtInput = new FilePathField(owner);
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
                var lbl = new JLabel("Exchange rates:");
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, panel1);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, 30, SpringLayout.NORTH, panel1);
                lbl.setOpaque(true);
                panel1.add(lbl);

                lblExchange = new JLabel();
                refreshExchange();
                panel1Layout.putConstraint(SpringLayout.WEST, lblExchange, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, lblExchange, 30, SpringLayout.NORTH, panel1);
                panel1.add(lblExchange);

                var lbl3 = Utils.createLinkLabel(owner, "edit...");
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
        }
        {
            table = new PaymentTable(transformInstruction, currencyConverter, Actor.Sender, validator);
            table.addProgressListener(progress -> {
                lblLoading.update(progress);
                cmdSendPayments.setEnabled(progress.isFinished());
            });
            table.addSelectorChangedListener(() -> cmdSendPayments.setEnabled(table.selectedPayments().length > 0));
            panel2.add(table);
        }
        {
            cmdSendPayments = new JButton("Send Payments");
            cmdSendPayments.setPreferredSize(new Dimension(150, 35));
            cmdSendPayments.setEnabled(false);
            cmdSendPayments.addActionListener(e -> sendPayments());
            panel3Layout.putConstraint(SpringLayout.EAST, cmdSendPayments, 0, SpringLayout.EAST, panel3);
            panel3.add(cmdSendPayments);

            lblLoading = new ProgressLabel();
            panel3Layout.putConstraint(SpringLayout.VERTICAL_CENTER, lblLoading, 0, SpringLayout.VERTICAL_CENTER, cmdSendPayments);
            panel3Layout.putConstraint(SpringLayout.EAST, lblLoading, -20, SpringLayout.WEST, cmdSendPayments);
            lblLoading.setOpaque(true);
            panel3.add(lblLoading);
        }
    }

    private void onTxtInputChanged() {
        var payments = new Payment[0];
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            var t = new TransactionTranslator(transformInstruction, currencyConverter);
            payments = t.apply(reader.read(new FileInputStream(txtInput.getText())));
        } catch (Exception e) {
            log.error(String.format("Could not read payments from %s", txtInput.getText()), e);
            ExceptionDialog.show(this, e, "Could not read payments from ISO 20022 pain.001 file.");
            return;
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            var br = new BalanceRefresher();
            br.refreshAllSenderWallets(payments);

            load(payments);

            try (var repo = new ConfigRepo()) {
                repo.setDefaultInputDirectory(txtInput.getCurrentDirectory());
                repo.commit();
            } catch (Exception e) {
                ExceptionDialog.show(this, e);
            }
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void sendPayments() {
        var payments = table.selectedPayments();
        if (payments.length == 0) {
            return;
        }

        if (!ValidationResultDialog.showErrorAndWarnings(this, table.getValidationResults(payments))) {
            return;
        }

        try {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                var br = new BalanceRefresher();
                br.refreshAllSenderWallets(payments);

                var ar = new AmountRefresher(payments);
                ar.refresh(transformInstruction.getExchangeRateProvider());
                var fr = new FeeRefresher(payments);
                fr.refresh();
            } finally {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            if (!showConfirmationForm(payments)) {
                return;
            }

            if (!askForPrivateKeyIfMissing(payments)) {
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            var results = validator.validate(payments);
            if (!ValidationResultDialog.showErrorAndWarnings(this, results)) {
                return;
            }

            transformInstruction.getLedger().send(PaymentConverter.toTransactions(payments));
            for (var p : payments) {
                table.refresh(p);
            }
        } catch (Exception ex) {
            ExceptionDialog.show(this, ex);
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private boolean askForPrivateKeyIfMissing(Payment[] payments) {
        var sendingWallets = PaymentUtils.distinctSendingWallets(payments);
        var privatKeyMissing = new ArrayList<Wallet>();
        for (var w : sendingWallets) {
            if (StringUtils.isAllEmpty(w.getSecret())) {
                privatKeyMissing.add(w);
            }
        }
        if (privatKeyMissing.size() == 0) {
            return true;
        }

        for (var w : privatKeyMissing) {
            var pnl = new JPanel();
            pnl.setLayout(new GridLayout(2, 1));

            var lbl = new JLabel(String.format("Enter secret / private Key for %s:", w.getPublicKey()));
            pnl.add(lbl);
            var pf = new JPasswordField();
            pnl.add(pf);

            var userOption = JOptionPane.showConfirmDialog(this, pnl, "Enter secret", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            var userInput = new String(pf.getPassword());
            if (JOptionPane.OK_OPTION != userOption || StringUtils.isAllEmpty(userInput)) {
                return false;
            }

            var ledger = PaymentUtils.getLedger(w, payments).orElseThrow();
            var vs = new WalletValidator(ledger).validateSecret(ledger.createWallet(w.getPublicKey(), userInput));
            if (vs != null) {
                ValidationResultDialog.show(this, new ValidationResult[]{vs});
                return false;
            }

            w.setSecret(userInput);

            // Apply value to same but not equal instances.
            for (var p : payments) {
                if (WalletCompare.isSame(p.getSenderWallet(), w)) {
                    p.getSenderWallet().setSecret(w.getSecret());
                }
            }
        }

        return true;
    }

    private void refreshExchange() {
        lblExchange.setText(transformInstruction.getExchangeRateProvider().getDisplayText());
    }

    private void showExchangeRateEdit() {
        var undefined = new HashMap<Payment, ExchangeRate>();
        var uniques = new HashMap<String, ExchangeRate>();
        for (var p : payments) {
            var r = p.getExchangeRate();
            if (r == null) {
                r = ExchangeRate.Undefined(p.createCcyPair());
                undefined.put(p, r);
            }
            uniques.put(r.getPair().getDisplayText(), r);
        }

        var frm = new ExchangeRatesForm(transformInstruction.getExchangeRateProvider(), uniques.values().toArray(new ExchangeRate[0]), ZonedDateTime.now());
        frm.setAllowChangeExchange(true);
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(400, 300);
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
            for (var item : undefined.entrySet()) {
                var p = item.getKey();
                if (ccyPairItem.getKey().equals(p.createCcyPair().getDisplayText())) {
                    p.setExchangeRate(ccyPairItem.getValue());
                }
            }
        }

        var ar = new AmountRefresher(payments);
        ar.refresh();

        loadTable(payments);
    }

    private boolean showConfirmationForm(Payment[] payments) {
        var frm = new SendConfirmationForm(payments, transformInstruction.getExchangeRateProvider(), this.payments.length);
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(600, 360);
        frm.setModal(true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
        if (!frm.isDialogAccepted()) {
            return false;
        }
        return true;
    }

    private void load(Payment[] payments) {
        if (payments == null) throw new IllegalArgumentException("Parameter 'payments' cannot be null");
        this.payments = payments;
        validator.getHistoryValidator().clearCache();
        validator.getHistoryValidator().loadHistory(payments);
        lblLoading.showLoading();
        loadTable(payments);
    }

    private void loadTable(Payment[] payments) {
        table.load(payments);
        cmdSendPayments.setEnabled(false);
    }

    public void setInput(String value) {
        txtInput.setText(value);
    }

    public void setReader(Pain001Reader reader) {
        this.reader = reader;
    }

    @Override
    public String getTitle() {
        return "Send Payments";
    }

    public void reload() {
        if (StringUtils.isEmpty(txtInput.getText())) {
            load(new Payment[0]);
            return;
        }

        onTxtInputChanged();
    }
}
