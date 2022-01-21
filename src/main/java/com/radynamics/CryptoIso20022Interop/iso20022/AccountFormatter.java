package com.radynamics.CryptoIso20022Interop.iso20022;

public class AccountFormatter {
    public static String format(Account a) {
        if (a instanceof IbanAccount) {
            var iban = (IbanAccount) a;
            return iban.getFormatted();
        } else if (a instanceof OtherAccount) {
            var iban = (OtherAccount) a;
            return iban.getUnformatted();
        } else {
            return a.toString();
        }
    }
}
