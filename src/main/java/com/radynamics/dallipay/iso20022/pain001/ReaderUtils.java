package com.radynamics.dallipay.iso20022.pain001;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.iso20022.Account;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public final class ReaderUtils {
    static Wallet toValidWalletOrNull(Ledger ledger, Account account) {
        if (account == null || !ledger.isValidPublicKey(account.getUnformatted())) {
            return null;
        }
        return ledger.createWallet(account.getUnformatted(), null);
    }

    static Optional<Component> getComponent(JPanel container, final String name) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            var component = container.getComponent(i);
            if (name.equals(component.getName())) {
                return Optional.of(component);
            }
        }
        return Optional.empty();
    }
}
