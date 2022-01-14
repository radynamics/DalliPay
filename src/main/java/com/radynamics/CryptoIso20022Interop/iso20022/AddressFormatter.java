package com.radynamics.CryptoIso20022Interop.iso20022;

import org.apache.commons.lang3.StringUtils;

public class AddressFormatter {
    public static String formatSingleLine(Address a) {
        var sb = new StringBuilder();
        sb.append(a.getName());

        if (!StringUtils.isAllEmpty(a.getCity())) {
            sb.append(",");
            if (!StringUtils.isAllEmpty(a.getZip())) {
                sb.append(String.format(" %s", a.getZip()));
            }
            sb.append(String.format(" %s", a.getCity()));
        }

        if (!StringUtils.isAllEmpty(a.getCountryShort())) {
            sb.append(String.format(" (%s)", a.getCountryShort()));
        }

        return sb.toString();
    }
}
