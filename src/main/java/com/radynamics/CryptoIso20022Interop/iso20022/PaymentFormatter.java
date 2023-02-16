package com.radynamics.CryptoIso20022Interop.iso20022;

import org.apache.commons.lang3.StringUtils;

public final class PaymentFormatter {
    public static String singleLineText(Account account, Address address) {
        var sb = new StringBuilder();

        if (address != null) {
            sb.append(AddressFormatter.formatSingleLine(address));
        }

        if (account == null && sb.length() > 0) {
            return sb.toString();
        }

        var accountText = account == null || StringUtils.isEmpty(account.getUnformatted())
                ? "Missing Account"
                : AccountFormatter.format(account);
        var template = sb.length() == 0 ? "%s" : " (%s)";
        sb.append(String.format(template, accountText));

        return sb.toString();
    }
}
