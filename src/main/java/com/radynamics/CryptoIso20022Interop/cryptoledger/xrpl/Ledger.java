package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api.JsonRpcApi;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.NotImplementedException;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;

public class Ledger implements com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger {
    private final WalletInfoProvider[] walletInfoProvider;
    private NetworkInfo network;

    public static final String ID = "xrpl";

    public Ledger() {
        walletInfoProvider = new WalletInfoProvider[]{
                new CachedWalletInfoProvider(new WalletInfoProvider[]{new LedgerWalletInfoProvider(this)})
        };
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getNativeCcySymbol() {
        return "XRP";
    }

    @Override
    public Transaction createTransaction() {
        return new Transaction(this, 0, getNativeCcySymbol());
    }

    @Override
    public void send(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction[] transactions) throws Exception {
        var api = new JsonRpcApi(this, network);
        api.send(transactions);
    }

    @Override
    public BigDecimal convertToNativeCcyAmount(long amountSmallestUnit) {
        return XrpCurrencyAmount.ofDrops(amountSmallestUnit).toXrp();
    }

    @Override
    public long convertToSmallestAmount(double amountNativeCcy) {
        // TODO: rounding is problematic, handle properly
        return Math.round(amountNativeCcy * 1000000);
    }

    @Override
    public FeeSuggestion getFeeSuggestion() {
        var api = new JsonRpcApi(this, network);
        var fees = api.latestFee();
        return fees == null ? FeeSuggestion.None() : fees.createSuggestion();
    }

    @Override
    public Wallet createWallet(String publicKey, String secret) {
        return new com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet(publicKey, secret);
    }

    @Override
    public void refreshBalance(Wallet wallet) {
        var api = new JsonRpcApi(this, network);
        api.refreshBalance(WalletConverter.from(wallet));
    }

    @Override
    public com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction[] listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception {
        var api = new JsonRpcApi(this, network);
        return api.listPaymentsReceived(WalletConverter.from(wallet), period);
    }

    public Transaction[] listTrustlineTransactions(com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet wallet, DateTimeRange period, Wallet ccyIssuer, String ccy) throws Exception {
        var api = new JsonRpcApi(this, network);
        return api.listTrustlineTransactions(wallet, period, WalletConverter.from(ccyIssuer), ccy);
    }

    @Override
    public boolean exists(Wallet wallet) {
        var api = new JsonRpcApi(this, network);
        return api.exists(WalletConverter.from(wallet));
    }

    @Override
    public boolean requiresDestinationTag(Wallet wallet) {
        var api = new JsonRpcApi(this, network);
        return api.requiresDestinationTag(WalletConverter.from(wallet));
    }

    public NetworkInfo getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(NetworkInfo network) {
        this.network = network;
    }

    @Override
    public WalletLookupProvider getLookupProvider() {
        return new Bithomp(network.getType());
    }

    @Override
    public TransactionLookupProvider getTransactionLookupProvider() {
        return new Bithomp(network.getType());
    }

    @Override
    public WalletInfoProvider[] getInfoProvider() {
        return walletInfoProvider;
    }

    @Override
    public boolean isValidPublicKey(String publicKey) {
        var addressCodec = new AddressCodec();
        return addressCodec.isValidClassicAddress(Address.of(publicKey));
    }

    @Override
    public HttpUrl getFallbackUrl(Network type) {
        switch (type) {
            case Live -> {
                return HttpUrl.get("https://xrplcluster.com/");
            }
            case Test -> {
                return HttpUrl.get("https://s.altnet.rippletest.net:51234/");
            }
            default -> throw new NotImplementedException(String.format("unknown network %s", type));
        }
    }
}
