package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import okhttp3.HttpUrl;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface Ledger {
    LedgerId getId();

    String getNativeCcySymbol();

    Transaction createTransaction();

    void send(Transaction[] transactions) throws Exception;

    BigDecimal convertToNativeCcyAmount(long amountSmallestUnit);

    FeeSuggestion getFeeSuggestion();

    Wallet createWallet(String publicKey, String secret);

    void refreshBalance(Wallet wallet);

    TransactionResult listPaymentsSent(Wallet wallet, ZonedDateTime since, int limit) throws Exception;

    TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception;

    boolean exists(Wallet wallet);

    ValidationResult[] validateReceiver(Wallet wallet);

    NetworkInfo getNetwork();

    void setNetwork(NetworkInfo network);

    WalletLookupProvider getLookupProvider();

    TransactionLookupProvider getTransactionLookupProvider();

    PaymentHistoryProvider getPaymentHistoryProvider();

    WalletInfoProvider[] getInfoProvider();

    void setInfoProvider(WalletInfoProvider[] walletInfoProvider);

    boolean isValidPublicKey(String publicKey);

    boolean isSecretValid(Wallet wallet);

    HttpUrl getFallbackUrl(Network type);
}
