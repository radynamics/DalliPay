package com.radynamics.dallipay.iso20022;

public interface IdGenerator {
    String createMsgId();
    String createStmId();
}
