package com.radynamics.dallipay.cryptoledger.xrpl;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.DestinationTagBuilder;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.generic.WalletConverter;
import com.radynamics.dallipay.cryptoledger.generic.WalletInput;
import com.radynamics.dallipay.cryptoledger.generic.WalletValidator;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.cryptoledger.signing.UserDialogPrivateKeyProvider;
import com.radynamics.dallipay.cryptoledger.xrpl.api.JsonRpcApi;
import com.radynamics.dallipay.cryptoledger.xrpl.walletinfo.Xumm;
import com.radynamics.dallipay.exchange.*;
import com.radynamics.dallipay.iso20022.camt054.AmountRounder;
import com.radynamics.dallipay.iso20022.camt054.LedgerCurrencyConverter;
import com.radynamics.dallipay.iso20022.camt054.LedgerCurrencyFormat;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.crypto.keys.Base58EncodedSecret;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Ledger implements com.radynamics.dallipay.cryptoledger.Ledger {
    private WalletInfoProvider[] walletInfoProvider;
    private TrustlineCache trustlineCache;
    private NetworkInfo network;
    private JsonRpcApi api;
    private WalletAddressResolver walletAddressResolver;

    public static final long AVG_LEDGER_CLOSE_TIME_SEC = 4;
    public static final UnsignedInteger APP_ID_TAG = UnsignedInteger.valueOf(20220613);
    public static final Integer NETWORKID_LIVENET = 0;
    public static final Integer NETWORKID_TESTNET = 1;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Validations");

    public Ledger() {
    }

    @Override
    public LedgerId getId() {
        return LedgerId.Xrpl;
    }

    @Override
    public String getNativeCcySymbol() {
        return "XRP";
    }

    @Override
    public Icon getIcon() {
        return new FlatSVGIcon("svg/xrpl.svg", 16, 13);
    }

    @Override
    public String getDisplayText() {
        return "XRP Ledger";
    }

    @Override
    public com.radynamics.dallipay.cryptoledger.generic.Transaction createTransaction() {
        return new com.radynamics.dallipay.cryptoledger.generic.Transaction(this, Money.zero(new Currency(getNativeCcySymbol())));
    }

    @Override
    public com.radynamics.dallipay.cryptoledger.Transaction getTransaction(String transactionId) {
        return api.getTransaction(transactionId);
    }

    public Money dropsToXrp(long drops) {
        return Money.of(XrpCurrencyAmount.ofDrops(drops).toXrp().doubleValue(), new Currency(getNativeCcySymbol()));
    }

    @Override
    public UnsignedLong toSmallestUnit(Money amount) {
        if (!amount.getCcy().getCode().equals(getNativeCcySymbol())) {
            throw new IllegalArgumentException("Amount expected in %s and not %s".formatted(getNativeCcySymbol(), amount.getCcy().getCode()));
        }
        return XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(amount.getNumber().doubleValue())).value();
    }

    @Override
    public FeeSuggestion getFeeSuggestion(com.radynamics.dallipay.cryptoledger.Transaction t) {
        var fees = api.latestFee();
        return fees == null ? FeeSuggestion.None(getNativeCcySymbol()) : fees.createSuggestion();
    }

    @Override
    public boolean equalTransactionFees() {
        return true;
    }

    @Override
    public WalletInput createWalletInput(String text) {
        return new WalletInput(this, text);
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
    public TransactionResult listPaymentsReceived(WalletInput wallet, DateTimeRange period) throws Exception {
        return api.listPaymentsReceived(WalletConverter.from(wallet.wallet()), period);
    }

    public com.radynamics.dallipay.cryptoledger.Transaction[] listTrustlineTransactions(com.radynamics.dallipay.cryptoledger.generic.Wallet wallet, DateTimeRange period, Wallet ccyIssuer, String ccy) throws Exception {
        return api.listTrustlineTransactions(wallet, period, WalletConverter.from(ccyIssuer), ccy);
    }

    public com.radynamics.dallipay.cryptoledger.Transaction[] listTrustlineTransactions(com.radynamics.dallipay.cryptoledger.generic.Wallet wallet, BlockRange period, Wallet ccyIssuer, String ccy) throws Exception {
        return api.listTrustlineTransactions(wallet, period, WalletConverter.from(ccyIssuer), ccy);
    }

    public Trustline[] listTrustlines(com.radynamics.dallipay.cryptoledger.generic.Wallet wallet) {
        return api.listTrustlines(wallet);
    }

    public String getAccountDomain(com.radynamics.dallipay.cryptoledger.generic.Wallet wallet) {
        return api.getAccountDomain(wallet);
    }

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
    public WalletValidator createWalletValidator() {
        return new XrplWalletValidator(this);
    }

    @Override
    public com.radynamics.dallipay.iso20022.PaymentValidator createPaymentValidator() {
        return new com.radynamics.dallipay.cryptoledger.xrpl.PaymentValidator(this, getTrustlineCache());
    }

    private TrustlineCache getTrustlineCache() {
        if (trustlineCache == null || trustlineCache.networkChanged(getNetwork())) {
            trustlineCache = new TrustlineCache(this);
        }
        return trustlineCache;
    }

    @Override
    public PaymentPathFinder createPaymentPathFinder() {
        return new com.radynamics.dallipay.cryptoledger.xrpl.paymentpath.PaymentPathFinder();
    }

    @Override
    public com.radynamics.dallipay.cryptoledger.generic.WalletAddressResolver createWalletAddressResolver() {
        if (walletAddressResolver == null) {
            walletAddressResolver = new WalletAddressResolver(this);
        }
        return walletAddressResolver;
    }

    @Override
    public WalletInfoProvider[] getInfoProvider() {
        if (walletInfoProvider == null) {
            walletInfoProvider = new WalletInfoProvider[]{
                    new CachedWalletInfoProvider(this, new WalletInfoProvider[]{
                            new StaticWalletInfoProvider(this), new LedgerWalletInfoProvider(this),
                            new Xumm()}),
                    new TrustlineInfoProvider(getTrustlineCache())
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
            if (StringUtils.isEmpty(wallet.getPublicKey())) {
                return true;
            }

            var seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of(wallet.getSecret()));
            var publicKeyText = seed.deriveKeyPair().publicKey().deriveAddress().value();
            return StringUtils.equals(publicKeyText, wallet.getPublicKey());
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isKnownMainnet(NetworkInfo network) {
        var url = network.getUrl().toString();
        return url.startsWith("https://xrplcluster.com")
                || url.startsWith("https://xrpl.ws")
                || url.startsWith("https://s1.ripple.com:51234")
                || url.startsWith("https://s2.ripple.com:51234");
    }

    @Override
    public NetworkInfo[] getDefaultNetworkInfo() {
        var networks = new NetworkInfo[2];
        networks[0] = NetworkInfo.createLivenet(HttpUrl.get("https://xrplcluster.com/"), "Mainnet");
        networks[0].setNetworkId(NETWORKID_LIVENET);
        networks[1] = NetworkInfo.createTestnet(HttpUrl.get("https://s.altnet.rippletest.net:51234/"), "Testnet");
        networks[1].setNetworkId(NETWORKID_TESTNET);
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
        return HttpUrl.get("https://faucet.altnet.rippletest.net");
    }

    @Override
    public PriceOracle[] getDefaultPriceOracles() {
        var list = new ArrayList<PriceOracle>();
        {
            var o = new PriceOracle("XRPL Labs Price Aggregator");
            list.add(o);
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "USD"), new com.radynamics.dallipay.cryptoledger.generic.Wallet(LedgerId.Xrpl, "r9PfV3sQpKLWxccdg3HL2FXKxGW2orAcLE"), new com.radynamics.dallipay.cryptoledger.generic.Wallet(LedgerId.Xrpl, "rXUMMaPpZqPutoRszR29jtC8amWq3APkx")));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "JPY"), new com.radynamics.dallipay.cryptoledger.generic.Wallet(LedgerId.Xrpl, "r9PfV3sQpKLWxccdg3HL2FXKxGW2orAcLE"), new com.radynamics.dallipay.cryptoledger.generic.Wallet(LedgerId.Xrpl, "rrJPYwVRyWFcwfaNMm83QEaCexEpKnkEg")));
        }
        {
            var o = new PriceOracle("radynamics Price Oracle");
            list.add(o);
            var issuer = new com.radynamics.dallipay.cryptoledger.generic.Wallet(LedgerId.Xrpl, "rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx");
            var receiver = new com.radynamics.dallipay.cryptoledger.generic.Wallet(LedgerId.Xrpl, "rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m");
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "USD"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "EUR"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "JPY"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "KRW"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "TRY"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "GBP"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "THB"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "RUB"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "BRL"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "AUD"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "MXN"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "ZAR"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "MYR"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "IDR"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "SGD"), issuer, receiver));
            o.add(new IssuedCurrency(new CurrencyPair("XRP", "CHF"), issuer, receiver));
        }
        return list.toArray(PriceOracle[]::new);
    }

    @Override
    public String getDefaultLookupProviderId() {
        return Bithomp.Id;
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
    public EndpointInfo getEndpointInfo(NetworkInfo networkInfo) throws Exception {
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
        if (!exists(sender) || !exists(receiver)) {
            return false;
        }
        return api.existsPath(WalletConverter.from(sender), WalletConverter.from(receiver), amount);
    }

    @Override
    public boolean existsSellOffer(Money minimum) {
        return api.existsSellOffer(minimum);
    }

    @Override
    public NetworkId[] networkIds() {
        return new NetworkId[]{
                new NetworkId(NETWORKID_LIVENET.toString(), "Mainnet"),
                new NetworkId(NETWORKID_TESTNET.toString(), "Testnet"),
        };
    }

    @Override
    public LedgerCurrencyConverter createLedgerCurrencyConverter(LedgerCurrencyFormat ledgerCurrencyFormat) {
        return new LedgerCurrencyConverter(new Currency(getNativeCcySymbol()), new Currency("Drop"), XrpCurrencyAmount.ofXrp(BigDecimal.ONE).value().longValue(), LedgerCurrencyFormat.Native, ledgerCurrencyFormat);
    }

    public TransactionSubmitter createRpcTransactionSubmitter(Component parentComponent) {
        return api.createTransactionSubmitter(new UserDialogPrivateKeyProvider(parentComponent));
    }
}
