package com.radynamics.dallipay.cryptoledger.generic;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;

public class WalletInput {
    private final Ledger ledger;
    private final String raw;
    private Wallet wallet;

    public WalletInput(Ledger ledger, String text) {
        this.ledger = ledger;
        this.raw = text;
    }

    public Wallet wallet() {
        if (wallet != null) {
            return wallet;
        }

        if (ledger.isValidPublicKey(raw)) {
            wallet = ledger.createWallet(raw, null);
            return wallet;
        }

        var addressInfo = ledger.createWalletAddressResolver().resolve(raw);
        if (addressInfo == null) {
            return null;
        }
        var result = ledger.createWalletValidator().validateFormat(addressInfo.getWallet());
        wallet = result == null ? addressInfo.getWallet() : null;
        return wallet;
    }

    public boolean valid() {
        return ledger.isValidPublicKey(raw) || wallet() != null;
    }

    public String raw() {
        return raw;
    }
}
