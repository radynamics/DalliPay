package com.radynamics.dallipay.cryptoledger.bitcoin;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.common.primitives.UnsignedLong;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.JsonRpcApi;
import com.radynamics.dallipay.cryptoledger.generic.WalletAddressInfo;
import com.radynamics.dallipay.cryptoledger.generic.WalletAddressResolver;
import com.radynamics.dallipay.cryptoledger.generic.WalletConverter;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.cryptoledger.signing.UserDialogPrivateKeyProvider;
import com.radynamics.dallipay.exchange.*;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.PaymentValidator;
import com.radynamics.dallipay.iso20022.camt054.AmountRounder;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.NotImplementedException;

import javax.swing.*;
import java.awt.*;

public class Ledger implements com.radynamics.dallipay.cryptoledger.Ledger {
    private WalletInfoProvider[] walletInfoProvider;
    private NetworkInfo network;
    private JsonRpcApi api;

    private final Long SATOSHI_PER_BTC = 100_000_000_000l;

    @Override
    public LedgerId getId() {
        return LedgerId.Bitcoin;
    }

    @Override
    public String getNativeCcySymbol() {
        return "BTC";
    }

    @Override
    public Icon getIcon() {
        return new FlatSVGIcon("svg/bitcoin.svg", 16, 16);
    }

    @Override
    public String getDisplayText() {
        return "Bitcoin";
    }

    @Override
    public Transaction createTransaction() {
        return new com.radynamics.dallipay.cryptoledger.generic.Transaction(this, Money.zero(new Currency(getNativeCcySymbol())));
    }

    @Override
    public Transaction getTransaction(String transactionId) {
        throw new NotImplementedException();
    }

    @Override
    public UnsignedLong toSmallestUnit(Money amount) {
        if (!amount.getCcy().getCode().equals(getNativeCcySymbol())) {
            throw new IllegalArgumentException("Amount expected in %s and not %s".formatted(getNativeCcySymbol(), amount.getCcy().getCode()));
        }
        return UnsignedLong.valueOf(Double.valueOf(amount.getNumber().doubleValue() * SATOSHI_PER_BTC).longValue());
    }

    @Override
    public FeeSuggestion getFeeSuggestion() {
        throw new NotImplementedException();
    }

    public Wallet createWallet(String publicKey) {
        return createWallet(publicKey, null);
    }

    @Override
    public Wallet createWallet(String publicKey, String secret) {
        return new com.radynamics.dallipay.cryptoledger.generic.Wallet(LedgerId.Bitcoin, publicKey, secret);
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
        return api.listPaymentsReceived(wallet, period);
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
        api = new JsonRpcApi(this, network);
    }

    @Override
    public PaymentHistoryProvider getPaymentHistoryProvider() {
        // TODO: implement
        return new PaymentHistoryProvider() {
            @Override
            public void load(com.radynamics.dallipay.cryptoledger.Ledger ledger, Wallet wallet, long sinceDaysAgo) {
                // do nothing
            }

            @Override
            public Transaction oldestSimilarOrDefault(Payment p) {
                return null;
            }
        };
    }

    @Override
    public ExchangeRateProvider createHistoricExchangeRateSource() {
        // TODO: implement
        return new DemoExchange();
    }

    @Override
    public PaymentValidator createPaymentValidator() {
        return new com.radynamics.dallipay.cryptoledger.bitcoin.PaymentValidator(this);
    }

    @Override
    public PaymentPathFinder createPaymentPathFinder() {
        return new com.radynamics.dallipay.cryptoledger.bitcoin.PaymentPathFinder();
    }

    @Override
    public WalletAddressResolver createWalletAddressResolver() {
        return value -> isValidPublicKey(value) ? new WalletAddressInfo(createWallet(value, null)) : null;
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
        return api.validateAddress(publicKey);
    }

    @Override
    public boolean isSecretValid(Wallet wallet) {
        throw new NotImplementedException();
    }

    @Override
    public NetworkInfo[] getDefaultNetworkInfo() {
        // TODO: implement
        var networks = new NetworkInfo[1];
        networks[0] = NetworkInfo.createTestnet(HttpUrl.get("http://user:pass@localhost:8332"), "Testnet");
        return networks;
    }

    @Override
    public String[] getExchangeRateProviders() {
        return new String[]{ManualRateProvider.ID, Coinbase.ID, Bitstamp.ID};
    }

    @Override
    public ExchangeRateProvider getDefaultExchangeRateProvider() {
        return ExchangeRateProviderFactory.create(Coinbase.ID, this);
    }

    @Override
    public HttpUrl getDefaultFaucetUrl() {
        return HttpUrl.get("https://bitcoinfaucet.uo1.net");
    }

    @Override
    public PriceOracle[] getDefaultPriceOracles() {
        return new PriceOracle[0];
    }

    @Override
    public String getDefaultLookupProviderId() {
        return MempoolSpace.Id;
    }

    @Override
    public TransactionSubmitterFactory createTransactionSubmitterFactory() {
        return new com.radynamics.dallipay.cryptoledger.bitcoin.TransactionSubmitterFactory(this);
    }

    @Override
    public Money roundNativeCcy(Money amt) {
        if (!amt.getCcy().getCode().equals(getNativeCcySymbol())) {
            throw new IllegalArgumentException(String.format("Currency must be %s instead of %s.", getNativeCcySymbol(), amt.getCcy()));
        }

        // Round to most accurate value supported by ledger.
        final int digits = 9;
        var rounded = AmountRounder.round(amt.getNumber().doubleValue(), digits);
        return Money.of(rounded.doubleValue(), amt.getCcy());
    }

    @Override
    public EndpointInfo getEndpointInfo(NetworkInfo networkInfo) {
        return api.getEndpointInfo(networkInfo);
    }

    @Override
    public boolean supportsDestinationTag() {
        return false;
    }

    @Override
    public DestinationTagBuilder createDestinationTagBuilder() {
        throw new NotImplementedException();
    }

    @Override
    public boolean existsPath(Wallet sender, Wallet receiver, Money amount) {
        return false;
    }

    @Override
    public boolean existsSellOffer(Money minimum) {
        return false;
    }

    public TransactionSubmitter createRpcTransactionSubmitter(Component parentComponent) {
        return api.createTransactionSubmitter(new UserDialogPrivateKeyProvider(parentComponent));
    }
}