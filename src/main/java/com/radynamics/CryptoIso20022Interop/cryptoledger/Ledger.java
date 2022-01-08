package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;

import java.math.BigDecimal;

public interface Ledger {
    String getNativeCcySymbol();

    Transaction createTransaction(Wallet sender, Wallet receiver, long amountSmallestUnit, String ccy);

    void send(Transaction[] transactions) throws Exception;

    BigDecimal convertToNativeCcyAmount(long amountSmallestUnit);

    long convertToSmallestAmount(double amountNativeCcy);

    Wallet createWallet(String publicKey, String secret);

    Transaction[] listPayments(Wallet wallet, DateTimeRange period) throws Exception;

    boolean exists(Wallet wallet);

    void setNetwork(Network network);
}
