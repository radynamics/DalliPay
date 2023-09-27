package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.generic.WalletAddressResolver;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.Money;
import okhttp3.HttpUrl;

import javax.swing.*;

public interface Ledger {
    LedgerId getId();

    String getNativeCcySymbol();

    Icon getIcon();

    String getDisplayText();

    Transaction createTransaction();

    Transaction getTransaction(String transactionId);

    FeeSuggestion getFeeSuggestion();

    Wallet createWallet(String publicKey, String secret);

    Wallet createRandomWallet(HttpUrl faucetUrl);

    void refreshBalance(Wallet wallet, boolean useCache);

    TransactionResult listPaymentsSent(Wallet wallet, long sinceDaysAgo, int limit) throws Exception;

    TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception;

    boolean exists(Wallet wallet);

    NetworkInfo getNetwork();

    void setNetwork(NetworkInfo network);

    PaymentHistoryProvider getPaymentHistoryProvider();

    ExchangeRateProvider createHistoricExchangeRateSource();

    com.radynamics.dallipay.iso20022.PaymentValidator createPaymentValidator();

    PaymentPathFinder createPaymentPathFinder();

    WalletAddressResolver createWalletAddressResolver();

    WalletInfoProvider[] getInfoProvider();

    void setInfoProvider(WalletInfoProvider[] walletInfoProvider);

    boolean isValidPublicKey(String publicKey);

    boolean isSecretValid(Wallet wallet);

    NetworkInfo[] getDefaultNetworkInfo();

    String[] getExchangeRateProviders();

    ExchangeRateProvider getDefaultExchangeRateProvider();

    HttpUrl getDefaultFaucetUrl();

    TransactionSubmitterFactory createTransactionSubmitterFactory();

    Money roundNativeCcy(Money amt);

    EndpointInfo getEndpointInfo(NetworkInfo networkInfo) throws Exception;

    boolean supportsDestinationTag();

    DestinationTagBuilder createDestinationTagBuilder();

    boolean existsPath(Wallet sender, Wallet receiver, Money amount);

    boolean existsSellOffer(Money minimum);
}
