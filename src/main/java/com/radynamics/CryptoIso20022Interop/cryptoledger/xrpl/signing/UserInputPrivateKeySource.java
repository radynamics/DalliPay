package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing;

import com.radynamics.CryptoIso20022Interop.cryptoledger.PaymentUtils;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletValidator;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.ui.ValidationResultDialog;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

public class UserInputPrivateKeySource implements PrivateKeyProvider {
    private final Component parentComponent;
    private final Hashtable<String, String> privateKeyCache = new Hashtable<>();

    public UserInputPrivateKeySource(Component parentComponent) {
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

            var lbl = new JLabel(String.format("Enter secret / private Key for %s:", w.getPublicKey()));
            pnl.add(lbl);
            var pf = new JPasswordField();
            pnl.add(pf);

            var userOption = JOptionPane.showConfirmDialog(parentComponent, pnl, "Enter secret", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            var userInput = new String(pf.getPassword());
            if (JOptionPane.OK_OPTION != userOption || StringUtils.isAllEmpty(userInput)) {
                return false;
            }

            var ledger = PaymentUtils.getLedger(w, payments).orElseThrow();
            var vs = new WalletValidator(ledger).validateSecret(ledger.createWallet(w.getPublicKey(), userInput));
            if (vs != null) {
                ValidationResultDialog.show(parentComponent, new ValidationResult[]{vs});
                return false;
            }

            privateKeyCache.put(w.getPublicKey(), userInput);
        }

        return true;
    }
}
