package com.radynamics.dallipay.iso20022;

import com.radynamics.dallipay.cryptoledger.Wallet;
import org.apache.commons.lang3.StringUtils;

public class AccountFactory {
    public static Account create(String value) {
        return IbanAccount.isValid(value)
                ? new IbanAccount(value)
                : new OtherAccount(value);
    }

    public static Account create(String value, Wallet fallback) {
        if (!StringUtils.isEmpty(value)) {
            return AccountFactory.create(value);
        }
        if (fallback != null && !StringUtils.isEmpty(fallback.getPublicKey())) {
            return AccountFactory.create(fallback.getPublicKey());
        }
        return null;
    }
}
