package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import okhttp3.HttpUrl;

import java.math.BigDecimal;

public interface Ledger {
    LedgerId getId();

    String getNativeCcySymbol();

    Transaction createTransaction();

    void send(Transaction[] transactions) throws Exception;

    BigDecimal convertToNativeCcyAmount(long amountSmallestUnit);

    long convertToSmallestAmount(double amountNativeCcy);

    FeeSuggestion getFeeSuggestion();

    Wallet createWallet(String publicKey, String secret);

    void refreshBalance(Wallet wallet);

    Transaction[] listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception;

    boolean exists(Wallet wallet);

    boolean requiresDestinationTag(Wallet wallet);

    void setNetwork(NetworkInfo network);

    WalletLookupProvider getLookupProvider();

    TransactionLookupProvider getTransactionLookupProvider();

    WalletInfoProvider[] getInfoProvider();

    boolean isValidPublicKey(String publicKey);

    HttpUrl getFallbackUrl(Network type);
}
