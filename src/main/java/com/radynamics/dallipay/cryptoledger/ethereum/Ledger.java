package com.radynamics.dallipay.cryptoledger.ethereum;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.Secrets;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.ethereum.api.AlchemyApi;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.cryptoledger.signing.UserDialogPrivateKeyProvider;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.DemoExchange;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.PaymentValidator;
import com.radynamics.dallipay.iso20022.camt054.AmountRounder;
import jakarta.ws.rs.NotSupportedException;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;

import javax.swing.*;
import java.awt.*;

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
    public Icon getIcon() {
        return new FlatSVGIcon("svg/ethereum.svg", 10, 16);
    }

    @Override
    public String getDisplayText() {
        return "Ethereum";
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
        var price = api.estimatedGasPrice();
        if (price == null) {
            return FeeSuggestion.None(getNativeCcySymbol());
        }

        return new FeeSuggestion(price.multiply(0.9d), price, price.multiply(1.1d));
    }

    public Wallet createWallet(String publicKey) {
        return createWallet(publicKey, null);
    }

    @Override
    public Wallet createWallet(String publicKey, String secret) {
        return new com.radynamics.dallipay.cryptoledger.generic.Wallet(getId(), publicKey, secret);
    }

    @Override
    public Wallet createRandomWallet(HttpUrl faucetUrl) {
        throw new NotImplementedException();
    }

    @Override
    public void refreshBalance(Wallet wallet, boolean useCache) {
        api.refreshBalance(wallet, useCache);
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
        return new com.radynamics.dallipay.cryptoledger.ethereum.PaymentValidator(this);
    }

    @Override
    public PaymentPathFinder createPaymentPathFinder() {
        return new com.radynamics.dallipay.cryptoledger.generic.paymentpath.PaymentPathFinder(
                new com.radynamics.dallipay.cryptoledger.ethereum.paymentpath.PaymentPathFinder()
        );
    }

    @Override
    public WalletInfoProvider[] getInfoProvider() {
        if (walletInfoProvider == null) {
            walletInfoProvider = new WalletInfoProvider[]{new StaticWalletInfoProvider(this)};
        }
        return walletInfoProvider;
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
        try {
            if (StringUtils.isEmpty(wallet.getSecret())) {
                return false;
            }
            var cred = Credentials.create(wallet.getSecret());
            // TODO: unclear why casing doesn't match
            return cred.getAddress().equalsIgnoreCase(wallet.getPublicKey());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public NetworkInfo[] getDefaultNetworkInfo() {
        var networks = new NetworkInfo[2];
        networks[0] = NetworkInfo.createLivenet(HttpUrl.get("https://eth-mainnet.g.alchemy.com/v2/" + Secrets.getAlchemyApiKeyEthereumMainnnet()));
        networks[1] = NetworkInfo.createTestnet(HttpUrl.get("https://eth-goerli.g.alchemy.com/v2/" + Secrets.getAlchemyApiKeyEthereumGoerli()));
        return networks;
    }

    @Override
    public HttpUrl getDefaultFaucetUrl() {
        return HttpUrl.get("https://goerlifaucet.com");
    }

    @Override
    public TransactionSubmitterFactory createTransactionSubmitterFactory() {
        return new com.radynamics.dallipay.cryptoledger.ethereum.TransactionSubmitterFactory(this);
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
