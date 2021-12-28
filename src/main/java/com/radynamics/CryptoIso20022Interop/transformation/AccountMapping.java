package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.iso20022.Account;

public class AccountMapping {
    private final Account account;
    public String walletPublicKey;

    public AccountMapping(Account account, String walletPublicKey) {
        if (account == null) throw new IllegalArgumentException("Parameter 'account' cannot be null");
        this.account = account;
        this.walletPublicKey = walletPublicKey;
    }

    public Account getAccount() {
        return account;
    }
}
