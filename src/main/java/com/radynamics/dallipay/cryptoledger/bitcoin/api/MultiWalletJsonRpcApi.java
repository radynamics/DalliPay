package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.bitcoin.Ledger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MultiWalletJsonRpcApi {
    final static Logger log = LogManager.getLogger(MultiWalletJsonRpcApi.class);
    private final Ledger ledger;
    private final NetworkInfo network;
    private final BitcoinJSONRPCClient genericClient;
    private final HashMap<String, BitcoinJSONRPCClient> walletClients = new HashMap<>();

    public MultiWalletJsonRpcApi(Ledger ledger, NetworkInfo network) {
        this.ledger = ledger;
        this.network = network;

        try {
            genericClient = new BitcoinJSONRPCClient(network.getUrl().url());
            for (var w : listwallets()) {
                walletClients.put(w, new BitcoinJSONRPCClient(new URL(network.getUrl().url() + "wallet/" + w)));
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> listwallets() {
        return (List<String>) genericClient.query("listwallets");
    }

    public List<BitcoindRpcClient.Transaction> listTransactions(String account, int count) {
        var list = new ArrayList<BitcoindRpcClient.Transaction>();
        for (var c : walletClients.values()) {
            list.addAll(c.listTransactions(account, count));
        }
        return list;
    }

    public List<BitcoindRpcClient.Transaction> listReceivedByAddress(Wallet wallet) {
        var list = new ArrayList<BitcoindRpcClient.Transaction>();
        for (var c : walletClients.values()) {
            for (var txId : listReceivedByAddress(c, wallet)) {
                list.add(c.getTransaction(txId));
            }
        }
        return list;
    }

    private ArrayList<String> listReceivedByAddress(BitcoinJSONRPCClient client, Wallet wallet) {
        final int minconf = 1;
        final boolean include_empty = true;
        final boolean include_watchonly = true;
        var result = (ArrayList<?>) client.query("listreceivedbyaddress", minconf, include_empty, include_watchonly, wallet.getPublicKey());

        var list = new ArrayList<String>();
        for (var r : result) {
            var map = ((LinkedHashMap) r);
            if (map.get("address").equals(wallet.getPublicKey())) {
                return (ArrayList<String>) map.get("txids");
            }
        }
        return list;
    }

    public ArrayList<Wallet> listWallets() {
        var list = new ArrayList<Wallet>();
        for (var c : walletClients.values()) {
            var labels = (List<String>) c.query("listlabels");
            for (var l : labels) {
                list.addAll(getAddressesByLabel(c, l));
            }
        }
        return list;
    }

    private List<Wallet> getAddressesByLabel(BitcoinJSONRPCClient client, String label) {
        var result = (LinkedHashMap<String, String>) client.query("getaddressesbylabel", label);
        var list = new ArrayList<Wallet>();
        for (var kvp : result.entrySet()) {
            list.add(ledger.createWallet(kvp.getKey()));
        }
        return list;
    }

    public BitcoindRpcClient.RawTransaction getRawTransaction(String txId) {
        return genericClient.getRawTransaction(txId);
    }

    public BitcoindRpcClient.DecodedScript decodeScript(String hex) {
        return genericClient.decodeScript(hex);
    }

    public BitcoindRpcClient.AddressValidationResult validateAddress(String address) {
        return genericClient.validateAddress(address);
    }

    public BigDecimal getBalance() {
        return genericClient.getBalance();
    }
}
