package com.radynamics.dallipay.cryptoledger.ethereum;

import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.exchange.DemoExchange;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.PaymentValidator;
import jakarta.ws.rs.NotSupportedException;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.NotImplementedException;

public class Ledger implements com.radynamics.dallipay.cryptoledger.Ledger {
    private WalletInfoProvider[] walletInfoProvider;
    private NetworkInfo network;

    @Override
    public LedgerId getId() {
        return LedgerId.Xrpl;
    }

    @Override
    public String getNativeCcySymbol() {
        return "ETH";
    }

    @Override
    public Transaction createTransaction() {
        throw new NotImplementedException();
    }

    @Override
    public Transaction getTransaction(String transactionId) {
        throw new NotImplementedException();
    }

    @Override
    public FeeSuggestion getFeeSuggestion() {
        throw new NotImplementedException();
    }

    @Override
    public Wallet createWallet(String publicKey, String secret) {
        return new com.radynamics.dallipay.cryptoledger.generic.Wallet(publicKey, secret);
    }

    @Override
    public Wallet createRandomWallet(HttpUrl faucetUrl) {
        throw new NotImplementedException();
    }

    @Override
    public void refreshBalance(Wallet wallet, boolean useCache) {
        throw new NotImplementedException();
    }

    @Override
    public TransactionResult listPaymentsSent(Wallet wallet, long sinceDaysAgo, int limit) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public boolean exists(Wallet wallet) {
        throw new NotImplementedException();
    }

    @Override
    public NetworkInfo getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(NetworkInfo network) {
        this.network = network;
        // TODO: api = new JsonRpcApi(...
    }

    @Override
    public PaymentHistoryProvider getPaymentHistoryProvider() {
        throw new NotImplementedException();
    }

    @Override
    public ExchangeRateProvider createHistoricExchangeRateSource() {
        // TODO: implement
        return new DemoExchange();
    }

    @Override
    public PaymentValidator createPaymentValidator() {
        throw new NotImplementedException();
    }

    @Override
    public PaymentPathFinder createPaymentPathFinder() {
        throw new NotImplementedException();
    }

    @Override
    public WalletInfoProvider[] getInfoProvider() {
        return new WalletInfoProvider[0];
    }

    @Override
    public void setInfoProvider(WalletInfoProvider[] walletInfoProvider) {
        this.walletInfoProvider = walletInfoProvider;
    }

    @Override
    public boolean isValidPublicKey(String publicKey) {
        // TODO: implement
        return false;
    }

    @Override
    public boolean isSecretValid(Wallet wallet) {
        // TODO: implement
        return false;
    }

    @Override
    public NetworkInfo[] getDefaultNetworkInfo() {
        // TODO: implement
        return new NetworkInfo[0];
    }

    @Override
    public HttpUrl getDefaultFaucetUrl() {
        return HttpUrl.get("https://goerlifaucet.com");
    }

    @Override
    public TransactionSubmitterFactory createTransactionSubmitterFactory() {
        throw new NotImplementedException();
    }

    @Override
    public Money roundNativeCcy(Money amt) {
        throw new NotImplementedException();
    }

    @Override
    public EndpointInfo getEndpointInfo(NetworkInfo networkInfo) {
        throw new NotImplementedException();
    }

    @Override
    public boolean supportsDestinationTag() {
        return false;
    }

    @Override
    public DestinationTagBuilder createDestinationTagBuilder() {
        throw new NotSupportedException();
    }
}
