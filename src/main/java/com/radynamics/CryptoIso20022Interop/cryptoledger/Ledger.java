package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import okhttp3.HttpUrl;

import java.math.BigDecimal;

public interface Ledger {
    String getId();

    String getNativeCcySymbol();

    Transaction createTransaction(Wallet sender, Wallet receiver, long amountSmallestUnit, String ccy);

    void send(Transaction[] transactions) throws Exception;

    BigDecimal convertToNativeCcyAmount(long amountSmallestUnit);

    long convertToSmallestAmount(double amountNativeCcy);

    Wallet createWallet(String publicKey, String secret);

    void refreshBalance(Wallet wallet);

    Transaction[] listPayments(Wallet wallet, DateTimeRange period) throws Exception;

    boolean exists(Wallet wallet);

    void setNetwork(NetworkInfo network);

    WalletLookupProvider getLookupProvider();

    boolean isValidPublicKey(String publicKey);

    HttpUrl getFallbackUrl(Network type);
}
