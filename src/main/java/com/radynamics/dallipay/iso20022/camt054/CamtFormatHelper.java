package com.radynamics.dallipay.iso20022.camt054;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;

public class CamtFormatHelper {
    private static ArrayList<CamtFormatEntry> entries;

    static {
        entries = new ArrayList<>();
        entries.add(new CamtFormatEntry(CamtFormat.Camt05300108, "camt05300108", "camt.053 Version 08"));
        entries.add(new CamtFormatEntry(CamtFormat.Camt05400102, "camt05400102", "camt.054 Version 02"));
        entries.add(new CamtFormatEntry(CamtFormat.Camt05400104, "camt05400104", "camt.054 Version 04"));
        entries.add(new CamtFormatEntry(CamtFormat.Camt05400109, "camt05400109", "camt.054 Version 09"));
    }

    public static final CamtFormat getDefault() {
        return CamtFormat.Camt05400104;
    }

    public static String toKey(CamtFormat value) {
        for (var o : entries) {
            if (o.getCamtFormat() == value) {
                return o.getKey();
            }
        }
        throw new NotImplementedException(String.format("Value %s unknown.", value));
    }

    public static CamtFormat toType(String value) {
        for (var o : entries) {
            if (o.getKey().equals(value)) {
                return o.getCamtFormat();
            }
        }
        throw new NotImplementedException(String.format("Value %s unknown.", value));
    }

    public static CamtFormatEntry[] all() {
        return entries.toArray(new CamtFormatEntry[0]);
    }
}
