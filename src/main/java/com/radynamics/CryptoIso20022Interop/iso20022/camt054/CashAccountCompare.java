package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.iso20022.Utils;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.generated.CashAccount25;
import org.apache.commons.lang3.StringUtils;

public class CashAccountCompare {
    static boolean isSame(CashAccount25 first, CashAccount25 second) {
        if (first == null) throw new IllegalArgumentException("Parameter 'first' cannot be null");
        if (second == null) throw new IllegalArgumentException("Parameter 'second' cannot be null");

        if (Utils.bothPresent(first.getId(), second.getId())) {
            if (!StringUtils.equalsIgnoreCase(first.getId().getIBAN(), second.getId().getIBAN())) {
                return false;
            }

            if (Utils.bothPresent(first.getId().getOthr(), second.getId().getOthr())) {
                if (!StringUtils.equalsIgnoreCase(first.getId().getOthr().getId(), second.getId().getOthr().getId())) {
                    return false;
                }
            } else {
                if (!Utils.bothNull(first.getId().getOthr(), second.getId().getOthr())) {
                    return false;
                }
            }
        } else {
            if (!Utils.bothNull(first.getId(), second.getId())) {
                return false;
            }
        }

        if (!StringUtils.equalsIgnoreCase(first.getCcy(), second.getCcy())) {
            return false;
        }
        if (!StringUtils.equalsIgnoreCase(first.getNm(), second.getNm())) {
            return false;
        }

        if (Utils.bothPresent(first.getTp(), second.getTp())) {
            if (!StringUtils.equalsIgnoreCase(first.getTp().getCd(), second.getTp().getCd())) {
                return false;
            }
            if (!StringUtils.equalsIgnoreCase(first.getTp().getPrtry(), second.getTp().getPrtry())) {
                return false;
            }

        } else {
            if (!Utils.bothNull(first.getTp(), second.getTp())) {
                return false;
            }
        }

        return true;
    }
}
