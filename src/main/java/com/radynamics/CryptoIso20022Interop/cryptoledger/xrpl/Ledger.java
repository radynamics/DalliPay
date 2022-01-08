package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api.JsonRpcApi;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;

public class Ledger implements com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger {
    private boolean isTestNet = true;
    private Network network;

    public static final String ID = "xrpl";

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
    public Wallet createWallet(String publicKey, String secret) {
        return new com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet(publicKey, secret);
    }

    @Override
    public com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction[] listPayments(Wallet wallet, DateTimeRange period) throws Exception {
        var api = new JsonRpcApi(this, network);
        return api.listPayments(WalletConverter.from(wallet), period);
    }

    @Override
    public boolean exists(Wallet wallet) {
        var api = new JsonRpcApi(this, network);
        return api.exists(WalletConverter.from(wallet));
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }
}
