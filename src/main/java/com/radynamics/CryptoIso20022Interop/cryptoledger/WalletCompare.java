package com.radynamics.CryptoIso20022Interop.cryptoledger;

import org.apache.commons.lang3.StringUtils;

public class WalletCompare {
    public static final boolean isSame(Wallet first, Wallet second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null && second != null) {
            return false;
        }
        if (first != null && second == null) {
            return false;
        }

        return StringUtils.equals(first.getPublicKey(), second.getPublicKey());
    }
}
