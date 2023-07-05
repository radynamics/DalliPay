package com.radynamics.dallipay.cryptoledger;

public interface Block {
    /**
     * Most recent validated block.
     */
    Block validated = () -> "validated";

    String getId();
}
