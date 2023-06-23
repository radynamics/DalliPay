package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.iso20022.Account;
import com.radynamics.dallipay.iso20022.AccountFactory;
import com.radynamics.dallipay.iso20022.IbanAccount;
import com.radynamics.dallipay.iso20022.OtherAccount;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ResourceBundle;

public class AccountField extends JPanel {
    private final static Logger log = LogManager.getLogger(WalletField.class);
    private final JComponent owner;
    private JTextField txt;
    private final AccountFieldInputValidator accountValidator = new AccountFieldInputValidator();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public AccountField(JComponent owner) {
        this.owner = owner;
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());

        {
            txt = new JTextField();
            txt.setColumns(28);
            txt.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("placeholderText"));
            txt.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    var text = ((JTextField) input).getText();
                    return accountValidator.isValid(text);
                }
            });
            txt.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    processFocusEvent(e);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    processFocusEvent(e);
                }
            });
            var c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridx = 0;
            c.gridy = 0;
            add(txt, c);
        }
    }

    public void setText(String value) {
        txt.setText(value);
        txt.getInputVerifier().verify(txt);
    }

    public String getText() {
        return txt.getText().trim();
    }

    public void setEditable(boolean b) {
        txt.setEditable(b);
        txt.setEnabled(b);
    }

    public void setAccount(Account account) {
        if (account == null) {
            setText("");
            return;
        }

        if (account instanceof IbanAccount) {
            var iban = (IbanAccount) account;
            setText(iban.getFormatted());
        } else if (account instanceof OtherAccount) {
            var iban = (OtherAccount) account;
            setText(iban.getUnformatted());
        } else {
            setText(account.toString());
        }
    }

    public Account getAccount(Wallet fallback) {
        var account = accountValidator.getValidOrNull(getText());
        if (account != null) {
            return account;
        }

        return AccountFactory.create(txt.getText(), fallback);
    }
}
