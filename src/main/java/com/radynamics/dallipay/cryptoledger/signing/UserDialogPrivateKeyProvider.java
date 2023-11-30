package com.radynamics.dallipay.cryptoledger.signing;

import com.radynamics.dallipay.cryptoledger.PaymentUtils;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.ui.ValidationResultDialog;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.ResourceBundle;

public class UserDialogPrivateKeyProvider implements PrivateKeyProvider {
    private final Component parentComponent;
    private final Hashtable<String, String> privateKeyCache = new Hashtable<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

    public UserDialogPrivateKeyProvider(Component parentComponent) {
        this.parentComponent = parentComponent;
    }

    @Override
    public String get(String publicKey) {
        return privateKeyCache.getOrDefault(publicKey, null);
    }

    @Override
    public boolean collect(Payment[] payments) {
        var sendingWallets = PaymentUtils.distinctSendingWallets(payments);
        if (sendingWallets.size() == 0) {
            return true;
        }

        for (var w : sendingWallets) {
            var pnl = new JPanel();
            pnl.setLayout(new GridLayout(2, 1));

            var lbl = new JLabel(String.format(res.getString("enterSecretSeed"), w.getPublicKey()));
            pnl.add(lbl);
            var pf = new JPasswordField();
            pnl.add(pf);

            var userOption = JOptionPane.showConfirmDialog(parentComponent, pnl, res.getString("enterSecretSeedTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            var userInput = new String(pf.getPassword());
            if (JOptionPane.OK_OPTION != userOption || StringUtils.isAllEmpty(userInput)) {
                return false;
            }

            var ledger = PaymentUtils.getLedger(w, payments).orElseThrow();
            var vs = ledger.createWalletValidator().validateSecret(ledger.createWallet(w.getPublicKey(), userInput));
            if (vs != null) {
                ValidationResultDialog.show(parentComponent, new ValidationResult[]{vs});
                return false;
            }

            privateKeyCache.put(w.getPublicKey(), userInput);
        }

        return true;
    }
}
