package com.radynamics.dallipay.cryptoledger.ethereum;

import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.ethereum.api.AlchemyApi;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.cryptoledger.xrpl.WalletConverter;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.DemoExchange;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.PaymentValidator;
import com.radynamics.dallipay.iso20022.camt054.AmountRounder;
import jakarta.ws.rs.NotSupportedException;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.NotImplementedException;

public class Ledger implements com.radynamics.dallipay.cryptoledger.Ledger {
    private WalletInfoProvider[] walletInfoProvider;
    private NetworkInfo network;
    private AlchemyApi api;

    @Override
    public LedgerId getId() {
        return LedgerId.Ethereum;
    }

    @Override
    public String getNativeCcySymbol() {
        return "ETH";
    }

    @Override
    public Transaction createTransaction() {
        return new com.radynamics.dallipay.cryptoledger.xrpl.Transaction(this, Money.zero(new Currency(getNativeCcySymbol())));
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
        api.refreshBalance(WalletConverter.from(wallet), useCache);
    }

    @Override
    public TransactionResult listPaymentsSent(Wallet wallet, long sinceDaysAgo, int limit) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception {
        return api.listPaymentsReceived(WalletConverter.from(wallet), period);
    }

    @Override
    public boolean exists(Wallet wallet) {
        return !wallet.getBalances().isEmpty();
    }

    @Override
    public NetworkInfo getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(NetworkInfo network) {
        this.network = network;
        api = new AlchemyApi(this, network);
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
        return new com.radynamics.dallipay.cryptoledger.ethereum.PaymentValidator(this);
    }

    @Override
    public PaymentPathFinder createPaymentPathFinder() {
        return new com.radynamics.dallipay.cryptoledger.ethereum.PaymentPathFinder();
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
        return org.web3j.crypto.WalletUtils.isValidAddress(publicKey);
    }

    @Override
    public boolean isSecretValid(Wallet wallet) {
        // TODO: implement
        return false;
    }

    @Override
    public NetworkInfo[] getDefaultNetworkInfo() {
        var networks = new NetworkInfo[1];
        networks[0] = NetworkInfo.createLivenet(HttpUrl.get("https://eth-mainnet.g.alchemy.com/v2/5eFF_YzV9d6i6Fo5EO89EwSp5RfS-vri"));
        return networks;
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
        if (!amt.getCcy().getCode().equals(getNativeCcySymbol())) {
            throw new IllegalArgumentException(String.format("Currency must be %s instead of %s.", getNativeCcySymbol(), amt.getCcy()));
        }

        // Round to most accurate value supported by ledger.
        final int digits = 18;
        var rounded = AmountRounder.round(amt.getNumber().doubleValue(), digits);
        return Money.of(rounded.doubleValue(), amt.getCcy());
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
