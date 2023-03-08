package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.Money;
import okhttp3.HttpUrl;

public interface Ledger {
    LedgerId getId();

    String getNativeCcySymbol();

    Transaction createTransaction();

    Transaction getTransaction(String transactionId);

    FeeSuggestion getFeeSuggestion();

    Wallet createWallet(String publicKey, String secret);

    Wallet createRandomWallet(HttpUrl faucetUrl);

    void refreshBalance(Wallet wallet, boolean useCache);

    TransactionResult listPaymentsSent(Wallet wallet, long sinceDaysAgo, int limit) throws Exception;

    TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception;

    boolean exists(Wallet wallet);

    ValidationResult[] validateReceiver(Wallet wallet);

    NetworkInfo getNetwork();

    void setNetwork(NetworkInfo network);

    PaymentHistoryProvider getPaymentHistoryProvider();

    ExchangeRateProvider createHistoricExchangeRateSource();

    com.radynamics.dallipay.iso20022.PaymentValidator createPaymentValidator();

    PaymentPathFinder createPaymentPathFinder();

    WalletInfoProvider[] getInfoProvider();

    void setInfoProvider(WalletInfoProvider[] walletInfoProvider);

    boolean isValidPublicKey(String publicKey);

    boolean isSecretValid(Wallet wallet);

    NetworkInfo[] getDefaultNetworkInfo();

    HttpUrl getDefaultFaucetUrl();

    TransactionSubmitterFactory createTransactionSubmitterFactory();

    Money roundNativeCcy(Money amt);

    EndpointInfo getEndpointInfo(NetworkInfo networkInfo);

    boolean supportsDestinationTag();

    DestinationTagBuilder createDestinationTagBuilder();
}
