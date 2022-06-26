package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api.JsonRpcApi;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Ledger implements com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger {
    private WalletInfoProvider[] walletInfoProvider;
    private NetworkInfo network;

    public Ledger() {
        walletInfoProvider = new WalletInfoProvider[]{
                new CachedWalletInfoProvider(this, new WalletInfoProvider[]{new LedgerWalletInfoProvider(this)})
        };
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
    public Transaction createTransaction() {
        return new Transaction(this, 0d, getNativeCcySymbol());
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
    public TransactionResult listPaymentsSent(Wallet wallet, ZonedDateTime since, int limit) throws Exception {
        var api = new JsonRpcApi(this, network);
        return api.listPaymentsSent(WalletConverter.from(wallet), since, limit);
    }

    @Override
    public TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception {
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
    public ValidationResult[] validateReceiver(Wallet wallet) {
        var list = new ArrayList<ValidationResult>();
        var xrplWallet = WalletConverter.from(wallet);

        var api = new JsonRpcApi(this, network);
        if (api.requiresDestinationTag(xrplWallet)) {
            list.add(new ValidationResult(ValidationState.Error, "Receiver wallet requires destination tag."));
        }
        if (!api.walletAccepts(xrplWallet, getNativeCcySymbol())) {
            list.add(new ValidationResult(ValidationState.Error, String.format("Receiver wallet disallows receiving %s", getNativeCcySymbol())));
        }
        if (api.isBlackholed(xrplWallet)) {
            list.add(new ValidationResult(ValidationState.Error, "Receiver wallet is blackholed. Amounts sent to this address will be lost."));
        }

        return list.toArray(new ValidationResult[0]);
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
    public PaymentHistoryProvider getPaymentHistoryProvider() {
        return new LedgerPaymentHistoryProvider();
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
            var sender = DefaultWalletFactory.getInstance().fromSeed(wallet.getSecret(), network.getType() != Network.Live);
            if (StringUtils.isEmpty(wallet.getPublicKey())) {
                return true;
            }
            return StringUtils.equals(sender.classicAddress().value(), wallet.getPublicKey());
        } catch (Exception ex) {
            return false;
        }
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
