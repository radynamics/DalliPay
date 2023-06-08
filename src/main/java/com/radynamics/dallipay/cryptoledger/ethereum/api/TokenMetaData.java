package com.radynamics.dallipay.cryptoledger.ethereum.api;

import com.radynamics.dallipay.cryptoledger.Wallet;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TokenMetaData {
    private final Wallet wallet;
    private final String symbol;
    private final int decimals;

    public TokenMetaData(Wallet wallet, String symbol, int decimals) {
        if (wallet == null) throw new IllegalArgumentException("Parameter 'wallet' cannot be null");
        if (symbol == null || symbol.length() == 0) throw new IllegalArgumentException("Parameter 'symbol' cannot be null or empty");
        this.wallet = wallet;
        this.symbol = symbol;
        this.decimals = decimals;
    }

    public Double getAmount(String hexEncodedAmount) {
        var amt = new BigInteger(hexEncodedAmount.startsWith("0x") ? hexEncodedAmount.substring(2) : hexEncodedAmount, 16);
        return new BigDecimal(amt).divide(BigDecimal.valueOf((long) Math.pow(10, decimals))).doubleValue();
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    @Override
    public String toString() {
        return "%s (%s), %s".formatted(symbol, decimals, wallet.getPublicKey());
    }
}
