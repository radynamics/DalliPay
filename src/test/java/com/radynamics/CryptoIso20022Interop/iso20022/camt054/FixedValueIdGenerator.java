package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.iso20022.IdGenerator;

public class FixedValueIdGenerator implements IdGenerator {
    private int stmIdCounter = 0;
    private String[] stmIds = {
            "STMID-3d0fa2c1-b1e7-4e7e-ad21-7d16132470df",
            "STMID-b403d698-6d3e-4614-ae43-14625b4619be",
            "STMID-2932e30c-7c8e-4508-ad8c-4f6952a00b1c",
    };

    @Override
    public String createMsgId() {
        return "MSGID-440fb60a-6752-465f-9369-55c2c470fe39";
    }

    @Override
    public String createStmId() {
        var value = stmIds[stmIdCounter];
        stmIdCounter++;
        return value;
    }
}