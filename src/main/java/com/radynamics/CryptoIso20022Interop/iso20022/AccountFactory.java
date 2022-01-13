package com.radynamics.CryptoIso20022Interop.iso20022;

public class AccountFactory {
    public static Account create(String value) {
        return IbanAccount.isValid(value)
                ? new IbanAccount(value)
                : new OtherAccount(value);
    }
}
