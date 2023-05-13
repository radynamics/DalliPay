package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.DestinationTagBuilder;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.cryptoledger.signing.UserDialogPrivateKeyProvider;
import com.radynamics.dallipay.cryptoledger.xrpl.api.JsonRpcApi;
import com.radynamics.dallipay.cryptoledger.xrpl.walletinfo.Xumm;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.ExchangeRateProviderFactory;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.camt054.AmountRounder;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;

import java.awt.*;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Ledger implements com.radynamics.dallipay.cryptoledger.Ledger {
    private WalletInfoProvider[] walletInfoProvider;
    private NetworkInfo network;
    private JsonRpcApi api;

    private static final String nativeCcySymbol = "XRP";

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Validations");

    public Ledger() {
    }

    @Override
    public LedgerId getId() {
        return LedgerId.Xrpl;
    }

    @Override
    public String getNativeCcySymbol() {
        return nativeCcySymbol;
    }

    @Override
    public Transaction createTransaction() {
        return new Transaction(this, Money.zero(new Currency(getNativeCcySymbol())));
    }

    @Override
    public com.radynamics.dallipay.cryptoledger.Transaction getTransaction(String transactionId) {
        return api.getTransaction(transactionId);
    }

    static Money dropsToXrp(long drops) {
        return Money.of(XrpCurrencyAmount.ofDrops(drops).toXrp().doubleValue(), new Currency(nativeCcySymbol));
    }

    @Override
    public FeeSuggestion getFeeSuggestion() {
        var fees = api.latestFee();
        return fees == null ? FeeSuggestion.None(getNativeCcySymbol()) : fees.createSuggestion();
    }

    @Override
    public Wallet createWallet(String publicKey, String secret) {
        return new com.radynamics.dallipay.cryptoledger.generic.Wallet(getId(), publicKey, secret);
    }

    @Override
    public Wallet createRandomWallet(HttpUrl faucetUrl) {
        return api.createRandomWallet(faucetUrl);
    }

    @Override
    public void refreshBalance(Wallet wallet, boolean useCache) {
        api.refreshBalance(WalletConverter.from(wallet), useCache);
    }

    @Override
    public TransactionResult listPaymentsSent(Wallet wallet, long sinceDaysAgo, int limit) throws Exception {
        return api.listPaymentsSent(WalletConverter.from(wallet), sinceDaysAgo, limit);
    }

    @Override
    public TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception {
        return api.listPaymentsReceived(WalletConverter.from(wallet), period);
    }

    public com.radynamics.dallipay.cryptoledger.Transaction[] listTrustlineTransactions(com.radynamics.dallipay.cryptoledger.generic.Wallet wallet, DateTimeRange period, Wallet ccyIssuer, String ccy) throws Exception {
        return api.listTrustlineTransactions(wallet, period, WalletConverter.from(ccyIssuer), ccy);
    }

    public Trustline[] listTrustlines(com.radynamics.dallipay.cryptoledger.generic.Wallet wallet) {
        return api.listTrustlines(wallet);
    }

    public String getAccountDomain(com.radynamics.dallipay.cryptoledger.generic.Wallet wallet) {
        return api.getAccountDomain(wallet);
    }

    @Override
    public boolean exists(Wallet wallet) {
        return api.exists(WalletConverter.from(wallet));
    }

    public boolean requiresDestinationTag(com.radynamics.dallipay.cryptoledger.generic.Wallet wallet) {
        return api.requiresDestinationTag(wallet);
    }

    public boolean isBlackholed(com.radynamics.dallipay.cryptoledger.generic.Wallet wallet) {
        return api.isBlackholed(wallet);
    }

    public boolean walletAcceptsXrp(Wallet wallet) {
        return api.walletAcceptsXrp(WalletConverter.from(wallet));
    }

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
        return new LedgerPaymentHistoryProvider();
    }

    @Override
    public ExchangeRateProvider createHistoricExchangeRateSource() {
        var livenet = Arrays.stream(getDefaultNetworkInfo()).filter(NetworkInfo::isLivenet).findFirst().orElseThrow();
        return ExchangeRateProviderFactory.create(XrplPriceOracle.ID, this, livenet);
    }

    @Override
    public com.radynamics.dallipay.iso20022.PaymentValidator createPaymentValidator() {
        return new com.radynamics.dallipay.cryptoledger.xrpl.PaymentValidator(this, new TrustlineCache(this));
    }

    @Override
    public PaymentPathFinder createPaymentPathFinder() {
        return new com.radynamics.dallipay.cryptoledger.xrpl.paymentpath.PaymentPathFinder();
    }

    @Override
    public WalletInfoProvider[] getInfoProvider() {
        if (walletInfoProvider == null) {
            walletInfoProvider = new WalletInfoProvider[]{
                    new CachedWalletInfoProvider(this, new WalletInfoProvider[]{
                            new StaticWalletInfoProvider(this), new LedgerWalletInfoProvider(this),
                            new Xumm()}),
                    new TrustlineInfoProvider(new TrustlineCache(this))
            };
        }
        return walletInfoProvider;
    }

    @Override
    public void setInfoProvider(WalletInfoProvider[] walletInfoProvider) {
        this.walletInfoProvider = walletInfoProvider;
    }

    @Override
    public boolean isValidPublicKey(String publicKey) {
        if (StringUtils.isEmpty(publicKey)) {
            return false;
        }
        var addressCodec = new AddressCodec();
        try {
            return addressCodec.isValidClassicAddress(Address.of(publicKey));
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean isSecretValid(Wallet wallet) {
        try {
            if (StringUtils.isEmpty(wallet.getSecret())) {
                return false;
            }
            var sender = DefaultWalletFactory.getInstance().fromSeed(wallet.getSecret(), network.isTestnet());
            if (StringUtils.isEmpty(wallet.getPublicKey())) {
                return true;
            }
            return StringUtils.equals(sender.classicAddress().value(), wallet.getPublicKey());
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public NetworkInfo[] getDefaultNetworkInfo() {
        var networks = new NetworkInfo[2];
        networks[0] = NetworkInfo.createLivenet(HttpUrl.get("https://xrplcluster.com/"));
        networks[1] = NetworkInfo.createTestnet(HttpUrl.get("https://s.altnet.rippletest.net:51234/"));
        return networks;
    }

    @Override
    public HttpUrl getDefaultFaucetUrl() {
        return HttpUrl.get("https://faucet.altnet.rippletest.net");
    }

    @Override
    public TransactionSubmitterFactory createTransactionSubmitterFactory() {
        return new com.radynamics.dallipay.cryptoledger.xrpl.TransactionSubmitterFactory(this);
    }

    @Override
    public Money roundNativeCcy(Money amt) {
        if (!amt.getCcy().getCode().equals(getNativeCcySymbol())) {
            throw new IllegalArgumentException(String.format("Currency must be %s instead of %s.", getNativeCcySymbol(), amt.getCcy()));
        }

        // Round to most accurate value supported by ledger.
        var digits = String.valueOf(CurrencyAmount.ONE_XRP_IN_DROPS).toCharArray().length - 1;
        var rounded = AmountRounder.round(amt.getNumber().doubleValue(), digits);
        return Money.of(rounded.doubleValue(), amt.getCcy());
    }

    @Override
    public EndpointInfo getEndpointInfo(NetworkInfo networkInfo) {
        return api.getEndpointInfo(networkInfo);
    }

    @Override
    public boolean supportsDestinationTag() {
        return true;
    }

    @Override
    public DestinationTagBuilder createDestinationTagBuilder() {
        return new com.radynamics.dallipay.cryptoledger.xrpl.DestinationTagBuilder();
    }

    @Override
    public boolean existsPath(Wallet sender, Wallet receiver, Money amount) {
        return api.existsPath(WalletConverter.from(sender), WalletConverter.from(receiver), amount);
    }

    public TransactionSubmitter createRpcTransactionSubmitter(Component parentComponent) {
        return api.createTransactionSubmitter(new UserDialogPrivateKeyProvider(parentComponent));
    }
}
