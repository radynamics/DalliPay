package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
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

    TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception;

    boolean exists(Wallet wallet);

    ValidationResult[] validateReceiver(Wallet wallet);

    NetworkInfo getNetwork();

    void setNetwork(NetworkInfo network);

    WalletLookupProvider getLookupProvider();

    TransactionLookupProvider getTransactionLookupProvider();

    WalletInfoProvider[] getInfoProvider();

    boolean isValidPublicKey(String publicKey);

    HttpUrl getFallbackUrl(Network type);
}
