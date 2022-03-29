package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import java.util.HashMap;

public class CamtFormatHelper {
    private static HashMap<String, CamtFormat> map;

    static {
        map = new HashMap<>();
        map.put("camt05400102", CamtFormat.Camt05400102);
        map.put("camt05400104", CamtFormat.Camt05400104);
        map.put("camt05400109", CamtFormat.Camt05400109);
    }

    public static final CamtFormat getDefault() {
        return CamtFormat.Camt05400104;
    }
}
