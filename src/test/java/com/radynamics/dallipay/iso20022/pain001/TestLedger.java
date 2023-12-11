package com.radynamics.dallipay.iso20022.pain001;

import com.google.common.primitives.UnsignedLong;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.generic.GenericWalletValidator;
import com.radynamics.dallipay.cryptoledger.generic.WalletAddressResolver;
import com.radynamics.dallipay.cryptoledger.generic.WalletInput;
import com.radynamics.dallipay.cryptoledger.generic.WalletValidator;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.ManualRateProvider;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.EmptyPaymentValidator;
import com.radynamics.dallipay.iso20022.camt054.LedgerCurrencyConverter;
import com.radynamics.dallipay.iso20022.camt054.LedgerCurrencyFormat;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;

public class TestLedger implements Ledger {
    private final static int FACTOR = 1000;
    private WalletInfoProvider[] walletInfoProvider = new WalletInfoProvider[0];
    private NetworkInfo network;

    private static final String nativeCcySymbol = "TEST";

    @Override
    public LedgerId getId() {
        return LedgerId.Xrpl;
    }

    @Override
    public String getNativeCcySymbol() {
        return nativeCcySymbol;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDisplayText() {
        return "Test";
    }

    @Override
    public Transaction createTransaction() {
        return new TestTransaction(this, 0d, getNativeCcySymbol());
    }

    @Override
    public Transaction getTransaction(String transactionId) {
        return null;
    }

    @Override
    public UnsignedLong toSmallestUnit(Money amount) {
        return UnsignedLong.valueOf(amount.getNumber().longValue() * FACTOR);
    }

    static Money convertToNativeCcyAmount(long amountSmallestUnit) {
        return Money.of((double) (amountSmallestUnit / FACTOR), new Currency(nativeCcySymbol));
    }

    @Override
    public FeeSuggestion getFeeSuggestion(Transaction t) {
        return new FeeSuggestion(convertToNativeCcyAmount(5), convertToNativeCcyAmount(10), convertToNativeCcyAmount(15));
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
        return new Wallet() {
            private final MoneyBag balances = new MoneyBag();

            @Override
            public String getPublicKey() {
                return publicKey;
            }

            @Override
            public String getSecret() {
                return secret;
            }

            @Override
            public void setSecret(String secret) {
                // do nothing
            }

            @Override
            public MoneyBag getBalances() {
                return balances;
            }

            @Override
            public LedgerId getLedgerId() {
                return getId();
            }
        };
    }

    @Override
    public Wallet createRandomWallet(HttpUrl faucetUrl) {
        var r = new Random();

        byte[] pk = new byte[10];
        r.nextBytes(pk);
        byte[] sk = new byte[10];
        r.nextBytes(sk);
        return createWallet(new String(pk, StandardCharsets.UTF_8), new String(sk, StandardCharsets.UTF_8));
    }

    @Override
    public void refreshBalance(Wallet wallet, boolean useCache) {
        // do nothing;
    }

    @Override
    public TransactionResult listPaymentsSent(Wallet wallet, long sinceDaysAgo, int limit) throws Exception {
        return new TransactionResult();
    }

    @Override
    public TransactionResult listPaymentsReceived(WalletInput wallet, DateTimeRange period) throws Exception {
        return new TransactionResult();
    }

    @Override
    public NetworkInfo getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(NetworkInfo network) {
        this.network = network;
    }

    @Override
    public PaymentHistoryProvider getPaymentHistoryProvider() {
        return null;
    }

    @Override
    public ExchangeRateProvider createHistoricExchangeRateSource() {
        return null;
    }

    @Override
    public WalletValidator createWalletValidator() {
        return new GenericWalletValidator(this);
    }

    @Override
    public com.radynamics.dallipay.iso20022.PaymentValidator createPaymentValidator() {
        return new EmptyPaymentValidator();
    }

    @Override
    public PaymentPathFinder createPaymentPathFinder() {
        return (currencyConverter, p) -> {
            var pp = new PaymentPath[1];
            pp[0] = new LedgerNativeCcyPath(currencyConverter, new Currency(getNativeCcySymbol()));
            return pp;
        };
    }

    @Override
    public WalletAddressResolver createWalletAddressResolver() {
        return value -> null;
    }

    @Override
    public WalletInfoProvider[] getInfoProvider() {
        return walletInfoProvider;
    }

    @Override
    public void setInfoProvider(WalletInfoProvider[] walletInfoProvider) {
        this.walletInfoProvider = walletInfoProvider;
    }

    @Override
    public boolean isValidPublicKey(String publicKey) {
        var map = new HashSet<String>();
        map.add("aaa");
        map.add("bbb");
        map.add("rwYb1M4hZcSG6tcAuhvgEwSpsiACKv6BG8");
        map.add("rNZtEviqTua4FcJebLkhq9hS7fkuxaodya");
        return map.contains(publicKey);
    }

    @Override
    public boolean isSecretValid(Wallet wallet) {
        return !StringUtils.isEmpty(wallet.getSecret());
    }

    @Override
    public NetworkInfo[] getDefaultNetworkInfo() {
        return new NetworkInfo[0];
    }

    @Override
    public String[] getExchangeRateProviders() {
        return new String[]{ManualRateProvider.ID};
    }

    @Override
    public ExchangeRateProvider getDefaultExchangeRateProvider() {
        return new ManualRateProvider();
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
        throw new NotImplementedException();
    }

    @Override
    public TransactionSubmitterFactory createTransactionSubmitterFactory() {
        throw new NotImplementedException();
    }

    @Override
    public Money roundNativeCcy(Money amt) {
        return amt;
    }

    @Override
    public EndpointInfo getEndpointInfo(NetworkInfo networkInfo) {
        return null;
    }

    @Override
    public boolean supportsDestinationTag() {
        return false;
    }

    @Override
    public DestinationTagBuilder createDestinationTagBuilder() {
        return null;
    }

    @Override
    public boolean existsPath(Wallet sender, Wallet receiver, Money amount) {
        return true;
    }

    @Override
    public boolean existsSellOffer(Money minimum) {
        return true;
    }

    @Override
    public NetworkId[] networkIds() {
        return new NetworkId[0];
    }

    @Override
    public LedgerCurrencyConverter createLedgerCurrencyConverter(LedgerCurrencyFormat ledgerCurrencyFormat) {
        return new LedgerCurrencyConverter(new Currency(getNativeCcySymbol()), new Currency("Testi"), FACTOR, LedgerCurrencyFormat.Native, ledgerCurrencyFormat);
    }
}
