package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.iso20022.Utils;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400102.generated.CashAccount20;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.generated.CashAccount25;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400109.generated.CashAccount41;
import org.apache.commons.lang3.StringUtils;

public class CashAccountCompare {
    public static boolean isSame(CashAccount41 first, CashAccount41 second) {
        return isSame(toCashAccount(first), toCashAccount(second));
    }

    public static boolean isSame(CashAccount25 first, CashAccount25 second) {
        return isSame(toCashAccount(first), toCashAccount(second));
    }

    public static boolean isSame(CashAccount20 first, CashAccount20 second) {
        return isSame(toCashAccount(first), toCashAccount(second));
    }

    private static CashAccount toCashAccount(CashAccount41 ca) {
        if (ca == null) throw new IllegalArgumentException("Parameter 'ca' cannot be null");
        return new CashAccount() {
            {
                id = ca.getId();
                idIban = ca.getId() == null ? null : ca.getId().getIBAN();
                idOthr = ca.getId() == null ? null : ca.getId().getOthr();
                idOthrId = ca.getId() == null || ca.getId().getOthr() == null ? null : ca.getId().getOthr().getId();
                ccy = ca.getCcy();
                nm = ca.getNm();
                tp = ca.getTp();
                tpCd = ca.getTp() == null ? null : ca.getTp().getCd();
                tpPrtry = ca.getTp() == null ? null : ca.getTp().getPrtry();
            }
        };
    }

    private static CashAccount toCashAccount(CashAccount25 ca) {
        if (ca == null) throw new IllegalArgumentException("Parameter 'ca' cannot be null");
        return new CashAccount() {
            {
                id = ca.getId();
                idIban = ca.getId() == null ? null : ca.getId().getIBAN();
                idOthr = ca.getId() == null ? null : ca.getId().getOthr();
                idOthrId = ca.getId() == null || ca.getId().getOthr() == null ? null : ca.getId().getOthr().getId();
                ccy = ca.getCcy();
                nm = ca.getNm();
                tp = ca.getTp();
                tpCd = ca.getTp() == null ? null : ca.getTp().getCd();
                tpPrtry = ca.getTp() == null ? null : ca.getTp().getPrtry();
            }
        };
    }

    private static CashAccount toCashAccount(CashAccount20 ca) {
        if (ca == null) throw new IllegalArgumentException("Parameter 'ca' cannot be null");
        return new CashAccount() {
            {
                id = ca.getId();
                idIban = ca.getId() == null ? null : ca.getId().getIBAN();
                idOthr = ca.getId() == null ? null : ca.getId().getOthr();
                idOthrId = ca.getId() == null || ca.getId().getOthr() == null ? null : ca.getId().getOthr().getId();
                ccy = ca.getCcy();
                nm = ca.getNm();
                tp = ca.getTp();
                tpCd = ca.getTp() == null || ca.getTp().getCd() == null ? null : ca.getTp().getCd().value();
                tpPrtry = ca.getTp() == null ? null : ca.getTp().getPrtry();
            }
        };
    }

    private static class CashAccount {
        public Object id;
        public String idIban;
        public Object idOthr;
        public String idOthrId;
        public String ccy;
        public String nm;
        public Object tp;
        public String tpCd;
        public String tpPrtry;
    }

    static boolean isSame(CashAccount first, CashAccount second) {
        if (Utils.bothPresent(first.id, second.id)) {
            if (!StringUtils.equalsIgnoreCase(first.idIban, second.idIban)) {
                return false;
            }

            if (Utils.bothPresent(first.idOthr, second.idOthr)) {
                if (!StringUtils.equalsIgnoreCase(first.idOthrId, second.idOthrId)) {
                    return false;
                }
            } else {
                if (!Utils.bothNull(first.idOthr, second.idOthr)) {
                    return false;
                }
            }
        } else {
            if (!Utils.bothNull(first.id, second.id)) {
                return false;
            }
        }

        if (!StringUtils.equalsIgnoreCase(first.ccy, second.ccy)) {
            return false;
        }
        if (!StringUtils.equalsIgnoreCase(first.nm, second.nm)) {
            return false;
        }

        if (Utils.bothPresent(first.tp, second.tp)) {
            if (!StringUtils.equalsIgnoreCase(first.tpCd, second.tpCd)) {
                return false;
            }
            if (!StringUtils.equalsIgnoreCase(first.tpPrtry, second.tpPrtry)) {
                return false;
            }

        } else {
            if (!Utils.bothNull(first.tp, second.tp)) {
                return false;
            }
        }

        return true;
    }
}
