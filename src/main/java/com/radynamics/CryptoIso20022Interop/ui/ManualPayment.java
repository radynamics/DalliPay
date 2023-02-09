package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.db.ConfigRepo;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.PaymentValidator;
import com.radynamics.CryptoIso20022Interop.transformation.FreeTextPaymentFactory;
import com.radynamics.CryptoIso20022Interop.transformation.TransactionTranslator;
import com.radynamics.CryptoIso20022Interop.ui.paymentTable.Actor;
import com.radynamics.CryptoIso20022Interop.util.RequestFocusListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class ManualPayment {
    private final static Logger log = LogManager.getLogger(ManualPayment.class);
    private final Payment payment;
    private final TransactionTranslator transactionTranslator;

    private ManualPayment(Payment payment, TransactionTranslator transactionTranslator) {
        if (payment == null) throw new IllegalArgumentException("Parameter 'payment' cannot be null");
        this.payment = payment;
        this.transactionTranslator = transactionTranslator;
    }

    public static ManualPayment createEmpty(Ledger ledger) {
        var payment = new Payment(ledger.createTransaction());
        payment.setAmount(Money.zero(new Currency(ledger.getNativeCcySymbol())));

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
        var userOption = JOptionPane.showConfirmDialog(parentComponent, new JScrollPane(txt), "Enter what you know or scan with a payment slip reader.", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (JOptionPane.OK_OPTION != userOption || txt.getText().length() == 0) {
            return null;
        }

        var factory = new FreeTextPaymentFactory(ledger);
        var payment = factory.createOrNull(txt.getText());
        if (payment == null) {
            JOptionPane.showMessageDialog(parentComponent, "Could not create a payment by given text. Please create a new payment manually instead.", "CryptoIso20022 Interop", JOptionPane.INFORMATION_MESSAGE);
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
    }

    private void applyAccountMapping() {
        transactionTranslator.apply(new Payment[]{payment});
    }

    public boolean show(Component parentComponent, PaymentValidator validator, ExchangeRateProvider exchangeRateProvider, CurrencyConverter currencyConverter) {
        var frm = PaymentDetailForm.showModal(parentComponent, payment, validator, exchangeRateProvider, currencyConverter, Actor.Sender);
        return frm.getPaymentChanged() && !payment.isAmountUnknown();
    }

    public Payment getPayment() {
        return payment;
    }
}
