package com.radynamics.dallipay.cryptoledger;

public interface Block {
    /**
     * Most recent validated block.
     */
    Block VALIDATED = () -> "validated";

    String getId();
}
