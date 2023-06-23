package com.radynamics.dallipay.iso20022;

import org.apache.commons.lang3.StringUtils;

public class AccountCompare {
    public static final boolean isSame(Account first, Account second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null && second != null) {
            return false;
        }
        if (first != null && second == null) {
            return false;
        }

        return StringUtils.equals(first.getUnformatted(), second.getUnformatted());
    }
}
