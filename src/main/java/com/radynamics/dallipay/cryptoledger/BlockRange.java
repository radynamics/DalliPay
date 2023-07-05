package com.radynamics.dallipay.cryptoledger;

public class BlockRange {
    private final Block start;
    private final Block end;

    private BlockRange(Block start, Block end) {
        if (start == null) throw new IllegalArgumentException("Parameter 'start' cannot be null");
        if (end == null) throw new IllegalArgumentException("Parameter 'end' cannot be null");
        this.start = start;
        this.end = end;
    }

    public static BlockRange of(Block start, Block end) {
        return new BlockRange(start, end);
    }

    public Block getStart() {
        return start;
    }

    public Block getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "%s - %s".formatted(start, end);
    }
}
