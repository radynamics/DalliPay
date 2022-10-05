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

    void refreshBalance(Wallet wallet);

    TransactionResult listPaymentsSent(Wallet wallet, ZonedDateTime since, int limit) throws Exception;

    TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception;

    boolean exists(Wallet wallet);

    ValidationResult[] validateReceiver(Wallet wallet);

    NetworkInfo getNetwork();

    void setNetwork(NetworkInfo network);

    PaymentHistoryProvider getPaymentHistoryProvider();

    ExchangeRateProvider createHistoricExchangeRateSource();

    WalletInfoProvider[] getInfoProvider();

    void setInfoProvider(WalletInfoProvider[] walletInfoProvider);

    boolean isValidPublicKey(String publicKey);

    boolean isSecretValid(Wallet wallet);

    NetworkInfo[] getDefaultNetworkInfo();
}
