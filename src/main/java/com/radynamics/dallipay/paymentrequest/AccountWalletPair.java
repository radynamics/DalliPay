package com.radynamics.dallipay.paymentrequest;

public class AccountWalletPair {
    private final String accountNo;
    private final String walletPublicKey;

    public AccountWalletPair(String accountNo, String walletPublicKey) {
        this.accountNo = accountNo;
        this.walletPublicKey = walletPublicKey;
    }

    public String accountNo() {
        return this.accountNo;
    }

    public String walletPublicKey() {
        return this.walletPublicKey;
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(accountNo, walletPublicKey);
    }
}
