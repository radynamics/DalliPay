package com.radynamics.dallipay.cryptoledger;

import com.google.common.primitives.UnsignedLong;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.generic.WalletAddressResolver;
import com.radynamics.dallipay.cryptoledger.generic.WalletInput;
import com.radynamics.dallipay.cryptoledger.generic.WalletValidator;
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

    UnsignedLong toSmallestUnit(Money amount);

    FeeSuggestion getFeeSuggestion();

    WalletInput createWalletInput(String text);

    Wallet createWallet(String publicKey, String secret);

    Wallet createRandomWallet(HttpUrl faucetUrl);

    void refreshBalance(Wallet wallet, boolean useCache);

    TransactionResult listPaymentsSent(Wallet wallet, long sinceDaysAgo, int limit) throws Exception;

    TransactionResult listPaymentsReceived(WalletInput walletInput, DateTimeRange period) throws Exception;

    NetworkInfo getNetwork();

    void setNetwork(NetworkInfo network);

    PaymentHistoryProvider getPaymentHistoryProvider();

    ExchangeRateProvider createHistoricExchangeRateSource();

    WalletValidator createWalletValidator();

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

    PriceOracle[] getDefaultPriceOracles();

    String getDefaultLookupProviderId();

    TransactionSubmitterFactory createTransactionSubmitterFactory();

    Money roundNativeCcy(Money amt);

    EndpointInfo getEndpointInfo(NetworkInfo networkInfo) throws Exception;

    boolean supportsDestinationTag();

    DestinationTagBuilder createDestinationTagBuilder();

    boolean existsPath(Wallet sender, Wallet receiver, Money amount);

    boolean existsSellOffer(Money minimum);
}
