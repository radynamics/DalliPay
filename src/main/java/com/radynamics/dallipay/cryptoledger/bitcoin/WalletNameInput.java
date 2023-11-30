package com.radynamics.dallipay.cryptoledger.bitcoin;

import com.radynamics.dallipay.cryptoledger.generic.WalletInput;

import java.util.List;

public class WalletNameInput extends WalletInput {
    private final List<String> walletNames;

    public WalletNameInput(Ledger ledger, String text, List<String> walletNames) {
        super(ledger, text);
        this.walletNames = walletNames;
    }

    @Override
    public boolean valid() {
        if (super.valid()) {
            return true;
        }

        // Also accept walletNames representing many wallet addresses.
        return walletNames.contains(raw());
    }
}
