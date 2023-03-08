package com.radynamics.dallipay.ui;

import com.alexandriasoftware.swing.JSplitButton;
import com.alexandriasoftware.swing.action.SplitButtonClickedActionListener;
import com.radynamics.dallipay.DateTimeConvert;
import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.cryptoledger.LookupProviderException;
import com.radynamics.dallipay.cryptoledger.LookupProviderFactory;
import com.radynamics.dallipay.cryptoledger.PaymentPath;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResultUtils;
import com.radynamics.dallipay.exchange.*;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.PaymentEdit;
import com.radynamics.dallipay.iso20022.PaymentFormatter;
import com.radynamics.dallipay.iso20022.PaymentValidator;
import com.radynamics.dallipay.ui.paymentTable.Actor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

public class PaymentDetailForm extends JDialog {
    private final Payment payment;
    private final PaymentEdit edit;
    private PaymentValidator validator;
    private ExchangeRateProvider exchangeRateProvider;
    private final CurrencyConverter currencyConverter;
    private final Actor actor;

    private SpringLayout panel1Layout;
    private JPanel pnlContent;
    private Component anchorComponentTopLeft;
    private boolean paymentChanged;
    private JLabel lblLedgerAmount;
    private MoneyTextField txtAmount;
    private WalletField txtSenderWallet;
    private WalletField txtReceiverWallet;
    private StructuredReferencesTextArea txtStructuredReferences;
    private JTextArea txtMessages;
    private JLabel lblEditExchangeRate;
    private JSplitButton cmdPaymentPath;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public PaymentDetailForm(Payment payment, PaymentValidator validator, ExchangeRateProvider exchangeRateProvider, CurrencyConverter currencyConverter, Actor actor, boolean editable) {
        if (payment == null) throw new IllegalArgumentException("Parameter 'payment' cannot be null");
        if (validator == null) throw new IllegalArgumentException("Parameter 'validator' cannot be null");
        if (exchangeRateProvider == null) throw new IllegalArgumentException("Parameter 'exchangeRateProvider' cannot be null");
        if (currencyConverter == null) throw new IllegalArgumentException("Parameter 'currencyConverter' cannot be null");
        this.payment = payment;
        this.validator = validator;
        this.exchangeRateProvider = exchangeRateProvider;
        this.currencyConverter = currencyConverter;
        this.actor = actor;

        edit = PaymentEdit.create(payment);
        edit.setEditable(editable);
        setupUI();
    }

    public static PaymentDetailForm showModal(Component c, Payment obj, PaymentValidator validator, ExchangeRateProvider exchangeRateProvider, CurrencyConverter currencyConverter, Actor actor, boolean editable) {
        var frm = new PaymentDetailForm(obj, validator, exchangeRateProvider, currencyConverter, actor, editable);
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(650, 495);
        frm.setModal(true);
        frm.setLocationRelativeTo(c);
        frm.setVisible(true);
        return frm;
    }

    private void setupUI() {
        setTitle(res.getString("title"));
        setIconImage(Utils.getProductIcon());

        var al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accept();
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
            int northPad = 0;
            final var lineHeight = 30;
            final var walletEditLineHeight = 55;
            final var walletEditOffsetNorth = 5;
            {
                var secondLine = new JPanel();
                secondLine.setLayout(new BoxLayout(secondLine, BoxLayout.X_AXIS));
                var pnlFirstLine = new JPanel();
                pnlFirstLine.setLayout(new BoxLayout(pnlFirstLine, BoxLayout.LINE_AXIS));
                txtAmount = new MoneyTextField(payment.getLedger());
                txtAmount.setEditable(edit.editable());
                txtAmount.addChangedListener(() -> onAmountChanged());
                pnlFirstLine.add(txtAmount);
                pnlFirstLine.add(Box.createRigidArea(new Dimension(10, 0)));
                {
                    cmdPaymentPath = new JSplitButton();
                    refreshPaymentPaths();
                    cmdPaymentPath.setVisible(actor == Actor.Sender);
                    cmdPaymentPath.setAlwaysPopup(true);
                    cmdPaymentPath.setPreferredSize(new Dimension(170, 21));
                    pnlFirstLine.add(cmdPaymentPath);
                }
                lblLedgerAmount = Utils.formatSecondaryInfo(new JLabel());

                var enabled = edit.exchangeRateEditable();
                lblEditExchangeRate = formatSecondLineLinkLabel(Utils.createLinkLabel(pnlContent, res.getString("edit"), enabled));
                refreshAmountsText();
                secondLine.add(lblLedgerAmount);
                {
                    lblEditExchangeRate.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (enabled && e.getClickCount() == 1) {
                                showExchangeRateEdit();
                            }
                        }
                    });
                    lblEditExchangeRate.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                    secondLine.add(lblEditExchangeRate);
                }
                anchorComponentTopLeft = createRow(northPad, res.getString("amount"), pnlFirstLine, secondLine, false, 7);
                northPad += lineHeight + 4;
            }
            {
                var lbl = new JLabel(PaymentFormatter.singleLineText(payment.getSenderAccount(), payment.getSenderAddress()));
                txtSenderWallet = new WalletField(pnlMain);
                txtSenderWallet.setLedger(payment.getLedger());
                txtSenderWallet.setWallet(payment.getSenderWallet());
                txtSenderWallet.setShowDetailVisible(true);
                txtSenderWallet.setInfoTextVisible(true);
                txtSenderWallet.setEditable(edit.editable());
                createRow(northPad, res.getString("sender"), lbl, txtSenderWallet, false, walletEditOffsetNorth);
                northPad += walletEditLineHeight;
            }
            {
                var lbl = new JLabel(PaymentFormatter.singleLineText(payment.getReceiverAccount(), payment.getReceiverAddress()));
                txtReceiverWallet = new WalletField(pnlMain);
                txtReceiverWallet.setLedger(payment.getLedger());
                txtReceiverWallet.setWallet(payment.getReceiverWallet());
                txtReceiverWallet.setDestinationTag(payment.getDestinationTag());
                txtReceiverWallet.setDestinationTagVisible(payment.getLedger().supportsDestinationTag());
                txtReceiverWallet.setShowDetailVisible(true);
                txtReceiverWallet.setInfoTextVisible(true);
                txtReceiverWallet.setEditable(edit.editable());
                createRow(northPad, res.getString("receiver"), lbl, txtReceiverWallet, false, walletEditOffsetNorth);
                northPad += walletEditLineHeight;
            }
            {
                JLabel secondLine = null;
                if (payment.getId() != null) {
                    secondLine = formatSecondLineLinkLabel(Utils.createLinkLabel(pnlContent, res.getString("showLedgerTrx")));
                    secondLine.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 1) {
                                showLedgerTransaction();
                            }
                        }
                    });
                }
                createRow(northPad, res.getString("booked"), payment.getBooked() == null ? res.getString("unknown") : Utils.createFormatDate().format(DateTimeConvert.toUserTimeZone(payment.getBooked())), secondLine);
                northPad += lineHeight;
            }
            {
                txtStructuredReferences = new StructuredReferencesTextArea();
                formatTextArea(txtStructuredReferences, 1);
                txtStructuredReferences.setValue(payment.getStructuredReferences());
                txtStructuredReferences.setEditable(edit.editable());
                createRow(northPad, res.getString("references"), new JScrollPane(txtStructuredReferences), null);
                northPad += lineHeight;
            }
            {
                txtMessages = createTextArea(3, Utils.toMultilineText(payment.getMessages()));
                Utils.patchTabBehavior(txtMessages);
                txtMessages.setEditable(edit.editable());
                createRow(northPad, res.getString("messages"), new JScrollPane(txtMessages), null);
                northPad += lineHeight * 2;
            }
            {
                var sb = new StringBuilder();
                if (payment.getTransmissionError() != null) {
                    sb.append(String.format("%s%s", payment.getTransmissionError().getMessage(), System.lineSeparator()));
                }
                var validations = validator.validate(payment);
                ValidationResultUtils.sortDescending(validations);
                for (var vr : validations) {
                    sb.append(String.format("- [%s] %s%s", vr.getStatus().name(), vr.getMessage(), System.lineSeparator()));
                }
                var pnl = new JPanel();
                pnl.setLayout(new BorderLayout());
                var txt = createTextArea(3, sb.length() == 0 ? "" : Utils.removeEndingLineSeparator(sb.toString()));
                pnl.add(new JScrollPane(txt));
                createRow(northPad, res.getString("issues"), pnl, null, true);
                northPad += lineHeight;
            }
        }
        {
            var cmd = new JButton(res.getString("close"));
            cmd.setPreferredSize(new Dimension(150, 35));
            cmd.addActionListener(e -> {
                accept();
            });
            panel3Layout.putConstraint(SpringLayout.EAST, cmd, 0, SpringLayout.EAST, panel3);
            panel3Layout.putConstraint(SpringLayout.SOUTH, cmd, 0, SpringLayout.SOUTH, panel3);
            panel3.add(cmd);
        }
    }

    private void accept() {
        if (edit.editable()) {
            applyUIValues();
        }
        close();
    }

    private void applyUIValues() {
        payment.setSenderWallet(txtSenderWallet.getWallet());
        payment.setReceiverWallet(txtReceiverWallet.getWallet());
        payment.setDestinationTag(txtReceiverWallet.getDestinationTag());
        payment.setStructuredReference(txtStructuredReferences.getValue());
        payment.setMessage(Utils.fromMultilineText(txtMessages.getText()));
        setPaymentChanged(true);
    }

    private void onAmountChanged() {
        payment.setAmount(txtAmount.getAmount());
        // Refresh paymentPath based on user entered currency. Eventually both sender/receiver use a common currency.
        payment.refreshPaymentPath(currencyConverter);

        // refresh all to set currently used as selected
        refreshPaymentPaths();
        var pair = new CurrencyPair(new Currency(payment.getLedger().getNativeCcySymbol()), payment.getUserCcy());
        payment.setExchangeRate(ExchangeRate.getOrNull(exchangeRateProvider.latestRates(), pair));
        refreshLedgerAmountsText();
    }

    private void refreshPaymentPaths() {
        JMenuItem selected = null;
        var popupMenu = new JPopupMenu();
        var availablePaths = payment.getLedger().createPaymentPathFinder().find(currencyConverter, payment);
        for (var path : availablePaths) {
            var item = new JMenuItem(path.getDisplayText());
            selected = path.isSet(payment) ? item : selected;
            popupMenu.add(item);
            item.addActionListener((SplitButtonClickedActionListener) e -> apply(path));
            txtAmount.addKnownCurrency(path.getCcy());
        }

        if (selected != null) {
            popupMenu.setSelected(selected);
            refreshPaymentPathText(selected.getText());
        }

        cmdPaymentPath.setEnabled(edit.editable() && availablePaths.length > 1 && payment.getBooked() == null);
        cmdPaymentPath.setPopupMenu(popupMenu);
    }

    private void apply(PaymentPath path) {
        path.apply(payment);
        refreshPaymentPathText(path.getDisplayText());
        refreshAmountsText();
        setPaymentChanged(true);
    }

    private void refreshPaymentPathText(String selectedText) {
        cmdPaymentPath.setText(String.format(res.getString("sendUsing"), selectedText));
    }

    private void refreshAmountsText() {
        txtAmount.setAmount(Money.of(payment.getAmount(), payment.getUserCcy()));
        refreshLedgerAmountsText();
    }

    private void refreshLedgerAmountsText() {
        lblLedgerAmount.setVisible(!payment.isUserCcyEqualTransactionCcy());
        lblEditExchangeRate.setVisible(!payment.isUserCcyEqualTransactionCcy());

        var amtLedgerText = MoneyFormatter.formatLedger(payment.getAmountTransaction());
        if (payment.getExchangeRate() == null) {
            lblLedgerAmount.setText(String.format("%s, " + res.getString("missingFxRate"), amtLedgerText));
            return;
        }

        var fxRateText = res.getString("unknown");
        var fxRateAtText = res.getString("unknown");
        if (!payment.isAmountUnknown()) {
            fxRateText = Utils.createFormatLedger().format(payment.getExchangeRate().getRate());
            fxRateAtText = Utils.createFormatDate().format(DateTimeConvert.toUserTimeZone(payment.getExchangeRate().getPointInTime()));
        }
        lblLedgerAmount.setText(String.format(res.getString("withFxRateOf"), amtLedgerText, fxRateText, fxRateAtText));
    }

    private void showExchangeRateEdit() {
        var rate = payment.getExchangeRate() == null ? ExchangeRate.Undefined(payment.createCcyPair()) : payment.getExchangeRate();

        var frm = new ExchangeRatesForm(exchangeRateProvider, new ExchangeRate[]{rate}, rate.getPointInTime());
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(500, 300);
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
        try {
            var lp = LookupProviderFactory.createTransactionLookupProvider(payment.getLedger());
            lp.open(payment.getId());
        } catch (LookupProviderException ex) {
            ExceptionDialog.show(this, ex);
        }
    }

    private JTextArea createTextArea(int rows, String text) {
        var txt = new JTextArea();
        formatTextArea(txt, rows);
        txt.setText(text);
        txt.setCaretPosition(0);
        return txt;
    }

    private static void formatTextArea(JTextArea txt, int rows) {
        txt.setRows(rows);
        txt.setColumns(39);
        txt.setLineWrap(true);
        txt.setEditable(false);
    }

    private void close() {
        dispose();
    }

    private Component createRow(int northPad, String labelText, Component firstLine, String contentSecondLine) {
        JLabel secondLine = null;
        if (contentSecondLine != null) {
            secondLine = new JLabel(contentSecondLine);
            Utils.formatSecondaryInfo(secondLine);
        }
        return createRow(northPad, labelText, firstLine, secondLine, false);
    }

    private Component createRow(int northPad, String labelText, String contentFirstLine, Component secondLine) {
        return createRow(northPad, labelText, new JLabel(contentFirstLine), secondLine, false);
    }

    private Component createRow(int northPad, String labelText, Component firstLine, Component secondLine, boolean growBottomRight) {
        return createRow(northPad, labelText, firstLine, secondLine, growBottomRight, 0);
    }

    private Component createRow(int northPad, String labelText, Component firstLine, Component secondLine, boolean growBottomRight, int secondLineNorthOffset) {
        var lbl = new JLabel(labelText);
        panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnlContent);
        panel1Layout.putConstraint(SpringLayout.NORTH, lbl, northPad, SpringLayout.NORTH, pnlContent);
        lbl.setOpaque(true);
        pnlContent.add(lbl);

        panel1Layout.putConstraint(SpringLayout.WEST, firstLine, 50, SpringLayout.EAST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
        panel1Layout.putConstraint(SpringLayout.NORTH, firstLine, northPad, SpringLayout.NORTH, pnlContent);
        if (growBottomRight) {
            panel1Layout.putConstraint(SpringLayout.EAST, pnlContent, 0, SpringLayout.EAST, firstLine);
            panel1Layout.putConstraint(SpringLayout.SOUTH, pnlContent, 0, SpringLayout.SOUTH, firstLine);
        }
        pnlContent.add(firstLine);

        if (secondLine != null) {
            panel1Layout.putConstraint(SpringLayout.WEST, secondLine, 50, SpringLayout.EAST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
            panel1Layout.putConstraint(SpringLayout.NORTH, secondLine, northPad + 13 + secondLineNorthOffset, SpringLayout.NORTH, pnlContent);
            pnlContent.add(secondLine);
        }

        return lbl;
    }

    private JLabel formatSecondLineLinkLabel(JLabel lbl) {
        lbl.putClientProperty("FlatLaf.styleClass", "small");
        return lbl;
    }

    private void setPaymentChanged(boolean changed) {
        this.paymentChanged = changed;
    }

    public boolean getPaymentChanged() {
        return paymentChanged;
    }
}
