package com.radynamics.dallipay.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.cryptoledger.Block;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

public class LedgerBlock implements Block {
    private final LedgerIndex ledgerIndex;

    public LedgerBlock(LedgerIndex ledgerIndex) {
        if (ledgerIndex == null) throw new IllegalArgumentException("Parameter 'ledgerIndex' cannot be null");
        this.ledgerIndex = ledgerIndex;
    }

    public LedgerBlock minus(UnsignedInteger offset) {
        return new LedgerBlock(ledgerIndex.minus(offset));
    }

    public LedgerBlock plus(UnsignedInteger offset) {
        return new LedgerBlock(ledgerIndex.plus(offset));
    }

    public long between(LedgerBlock block) {
        if (block == null) throw new IllegalArgumentException("Parameter 'block' cannot be null");
        return ledgerIndex.unsignedIntegerValue().longValue() - block.ledgerIndex.unsignedIntegerValue().longValue();
    }

    @Override
    public String getId() {
        return ledgerIndex.value();
    }

    public LedgerIndex getLedgerIndex() {
        return ledgerIndex;
    }
}
