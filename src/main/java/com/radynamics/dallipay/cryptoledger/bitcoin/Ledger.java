package com.radynamics.dallipay.cryptoledger.bitcoin;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.common.primitives.UnsignedLong;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.ApiException;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.BitcoinCoreWalletImport;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.JsonRpcApi;
import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.Device;
import com.radynamics.dallipay.cryptoledger.bitcoin.signing.BitcoinCoreRpcSubmitter;
import com.radynamics.dallipay.cryptoledger.generic.*;
import com.radynamics.dallipay.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.cryptoledger.signing.UserDialogPrivateKeyProvider;
import com.radynamics.dallipay.exchange.*;
import com.radynamics.dallipay.iso20022.PaymentValidator;
import com.radynamics.dallipay.iso20022.camt054.AmountRounder;
import com.radynamics.dallipay.iso20022.camt054.LedgerCurrencyConverter;
import com.radynamics.dallipay.iso20022.camt054.LedgerCurrencyFormat;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.NotImplementedException;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ledger implements com.radynamics.dallipay.cryptoledger.Ledger {
    private WalletInfoProvider[] walletInfoProvider;
    private NetworkInfo network;
    private JsonRpcApi api;

    private final Long SATOSHI_PER_BTC = 100_000_000l;
    public static final Integer NETWORKID_MAINNET = 0;
    public static final Integer NETWORKID_TESTNET = 1;

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
    public FeeSuggestion getFeeSuggestion(Transaction t) {
        var ccy = new Currency(getNativeCcySymbol());
        var low = Money.of(api.estimateSmartFee(6).doubleValue(), ccy);
        var medium = Money.of(api.estimateSmartFee(3).doubleValue(), ccy);
        var high = Money.of(api.estimateSmartFee(1).doubleValue(), ccy);
        return new FeeSuggestion(low, medium, high);
    }

    @Override
    public boolean equalTransactionFees() {
        return false;
    }

    @Override
    public WalletInput createWalletInput(String text) {
        if (!networkConfigured()) {
            return new WalletInput(this, text);
        }
        return new WalletNameInput(this, text, api.walletNames());
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
        return api.listPaymentsSent(WalletConverter.from(wallet), sinceDaysAgo, limit);
    }

    @Override
    public TransactionResult listPaymentsReceived(WalletInput walletInput, DateTimeRange period) throws Exception {
        return api.listPaymentsReceived(walletInput, period);
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
        return new LedgerPaymentHistoryProvider();
    }

    @Override
    public ExchangeRateProvider createHistoricExchangeRateSource() {
        return ExchangeRateProviderFactory.create(CryptoPriceOracle.ID, this);
    }

    @Override
    public WalletValidator createWalletValidator() {
        return new GenericWalletValidator(this);
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
        if (!networkConfigured()) {
            return true;
        }
        return api.validateAddress(publicKey);
    }

    private boolean networkConfigured() {
        // Null for fresh Bitcoin usage if no node connection has been configured yet.
        return getNetwork() != null;
    }

    @Override
    public boolean isSecretValid(Wallet wallet) {
        return api.isValidWalletPassPhrase(wallet);
    }

    @Override
    public NetworkInfo[] getDefaultNetworkInfo() {
        return new NetworkInfo[0];
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
        return null;
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
        final int digits = BigDecimal.valueOf(1d / SATOSHI_PER_BTC).scale() - 1;
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

    @Override
    public NetworkId[] networkIds() {
        return new NetworkId[]{
                new NetworkId(NETWORKID_MAINNET.toString(), "Mainnet"),
                new NetworkId(NETWORKID_TESTNET.toString(), "Testnet"),
        };
    }

    @Override
    public LedgerCurrencyConverter createLedgerCurrencyConverter(LedgerCurrencyFormat ledgerCurrencyFormat) {
        return new LedgerCurrencyConverter(new Currency(getNativeCcySymbol()), new Currency("Sat"), SATOSHI_PER_BTC, LedgerCurrencyFormat.SmallestUnit, ledgerCurrencyFormat);
    }

    @Override
    public WalletSetupProcess createWalletSetupProcess(Component parentComponent) {
        return new BitcoinCoreWalletImport(parentComponent, this);
    }

    public BitcoinCoreRpcSubmitter createRpcTransactionSubmitter(Component parentComponent) {
        return api.createTransactionSubmitter(new UserDialogPrivateKeyProvider(parentComponent));
    }

    public BitcoinCoreRpcSubmitter createRpcTransactionSubmitter(PrivateKeyProvider privateKeyProvider) {
        return api.createTransactionSubmitter(privateKeyProvider);
    }

    public void importWallet(String walletName, LocalDateTime historicTransactionSince, Wallet wallet) throws ApiException {
        api.importWallet(walletName, historicTransactionSince, wallet);
    }

    public void importWallet(String walletName, LocalDateTime historicTransactionSince, Device device) throws ApiException {
        api.importWallet(walletName, historicTransactionSince, device);
    }
}
