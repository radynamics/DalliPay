package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.BalanceRefresher;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.transaction.Origin;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.pain001.PaymentValidator;
import com.radynamics.dallipay.transformation.FreeTextPaymentFactory;
import com.radynamics.dallipay.transformation.TransactionTranslator;
import com.radynamics.dallipay.ui.paymentTable.Actor;
import com.radynamics.dallipay.util.RequestFocusListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class ManualPayment {
    private final static Logger log = LogManager.getLogger(ManualPayment.class);
    private final Payment payment;
    private final TransactionTranslator transactionTranslator;

    private static final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    private ManualPayment(Payment payment, TransactionTranslator transactionTranslator) {
        if (payment == null) throw new IllegalArgumentException("Parameter 'payment' cannot be null");
        this.payment = payment;
        this.transactionTranslator = transactionTranslator;
    }

    public static ManualPayment createEmpty(Ledger ledger) {
        var payment = new Payment(ledger.createTransaction());
        payment.setAmount(Money.zero(new Currency(ledger.getNativeCcySymbol())));
        payment.setOrigin(Origin.Manual);

        var o = new ManualPayment(payment, null);
        o.applyDefaultSenderWallet();
        return o;
    }

    public static ManualPayment createByFreeText(Component parentComponent, Ledger ledger, TransactionTranslator transactionTranslator) {
        var txt = new JTextArea();
        Utils.patchTabBehavior(txt);
        txt.setColumns(30);
        txt.setRows(15);
        txt.setSize(txt.getPreferredSize().width, txt.getPreferredSize().height);
        txt.addAncestorListener(new RequestFocusListener());
        var userOption = JOptionPane.showConfirmDialog(parentComponent, new JScrollPane(txt), res.getString("manualPayment.freeText"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (JOptionPane.OK_OPTION != userOption || txt.getText().length() == 0) {
            return null;
        }

        var factory = new FreeTextPaymentFactory(ledger);
        var payment = factory.createOrNull(txt.getText());
        if (payment == null) {
            JOptionPane.showMessageDialog(parentComponent, res.getString("manualPayment.failed"), "DalliPay", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        var o = new ManualPayment(payment, transactionTranslator);
        o.applyDefaultSenderWallet();
        o.applyAccountMapping();
        return o;
    }

    private void applyDefaultSenderWallet() {
        try (var repo = new ConfigRepo()) {
            payment.setSenderWallet(repo.getDefaultSenderWallet(payment.getLedger()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (payment.getSenderWallet() != null) {
            var br = new BalanceRefresher(payment.getLedger().getNetwork());
            br.refresh(payment);
        }
    }

    private void applyAccountMapping() {
        transactionTranslator.apply(new Payment[]{payment});
    }

    public boolean show(Component parentComponent, PaymentValidator validator, ExchangeRateProvider exchangeRateProvider, CurrencyConverter currencyConverter) {
        var frm = PaymentDetailForm.showModal(parentComponent, payment, validator, exchangeRateProvider, currencyConverter, Actor.Sender, true);
        return frm.getPaymentChanged() && !payment.isAmountUnknown();
    }

    public Payment getPayment() {
        return payment;
    }
}
