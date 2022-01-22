package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletLookupProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api.JsonRpcApi;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;

public class Ledger implements com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger {
    private boolean isTestNet = true;
    private NetworkInfo network;

    public static final String ID = "xrpl";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getNativeCcySymbol() {
        return "XRP";
    }

    @Override
    public Transaction createTransaction(Wallet sender, Wallet receiver, long amountSmallestUnit, String ccy) {
        var t = new Transaction(this, amountSmallestUnit, ccy);
        if (sender != null) {
            t.setSender(WalletConverter.from(sender));
        }
        if (receiver != null) {
            t.setReceiver(WalletConverter.from(receiver));
        }

        return t;
    }

    @Override
    public void send(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction[] transactions) throws Exception {
        try {
            var api = new JsonRpcApi(this, network);
            api.send(transactions);
        } finally {
            for (var t : transactions) {
                ((Transaction) t).setTransmission(StringUtils.isAllEmpty(t.getId()) ? TransmissionState.Error : TransmissionState.Success);
            }
        }
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
    public Wallet createWallet(String publicKey, String secret) {
        return new com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet(publicKey, secret);
    }

    @Override
    public void refreshBalance(Wallet wallet) {
        var api = new JsonRpcApi(this, network);
        api.refreshBalance(WalletConverter.from(wallet));
    }

    @Override
    public com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction[] listPayments(Wallet wallet, DateTimeRange period) throws Exception {
        var api = new JsonRpcApi(this, network);
        return api.listPayments(WalletConverter.from(wallet), period);
    }

    public Transaction[] listTransactions(com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet wallet, DateTimeRange period) throws Exception {
        var api = new JsonRpcApi(this, network);
        return api.listTransactions(wallet, period);
    }

    @Override
    public boolean exists(Wallet wallet) {
        var api = new JsonRpcApi(this, network);
        return api.exists(WalletConverter.from(wallet));
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
