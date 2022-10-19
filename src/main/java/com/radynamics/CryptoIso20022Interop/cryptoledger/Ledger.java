package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;

import java.time.ZonedDateTime;

public interface Ledger {
    LedgerId getId();

    String getNativeCcySymbol();

    Transaction createTransaction();

    void send(Transaction[] transactions) throws Exception;

    FeeSuggestion getFeeSuggestion();

    Wallet createWallet(String publicKey, String secret);

    void refreshBalance(Wallet wallet, boolean useCache);

    TransactionResult listPaymentsSent(Wallet wallet, long sinceDaysAgo, int limit) throws Exception;

    TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception;

    boolean exists(Wallet wallet);

    ValidationResult[] validateReceiver(Wallet wallet);

    NetworkInfo getNetwork();

    void setNetwork(NetworkInfo network);

    PaymentHistoryProvider getPaymentHistoryProvider();

    ExchangeRateProvider createHistoricExchangeRateSource();

    com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator createPaymentValidator();

    WalletInfoProvider[] getInfoProvider();

    void setInfoProvider(WalletInfoProvider[] walletInfoProvider);

    boolean isValidPublicKey(String publicKey);

    boolean isSecretValid(Wallet wallet);

    NetworkInfo[] getDefaultNetworkInfo();
}
