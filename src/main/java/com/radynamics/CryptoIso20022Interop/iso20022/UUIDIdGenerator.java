package com.radynamics.CryptoIso20022Interop.iso20022;

import java.util.UUID;

public class UUIDIdGenerator implements IdGenerator {
    @Override
    public String createMsgId() {
        return "MSG" + format(UUID.randomUUID());
    }

    @Override
    public String createStmId() {
        return "STM" + format(UUID.randomUUID());
    }

    private String format(UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }
}
