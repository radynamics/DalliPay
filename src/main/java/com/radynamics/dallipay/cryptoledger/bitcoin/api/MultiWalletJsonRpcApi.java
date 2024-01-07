package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletCompare;
import com.radynamics.dallipay.cryptoledger.bitcoin.Ledger;
import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRPCException;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.krotjson.JSON;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class MultiWalletJsonRpcApi {
    final static Logger log = LogManager.getLogger(MultiWalletJsonRpcApi.class);
    private final Ledger ledger;
    private final NetworkInfo network;
    private BitcoinJSONRPCClient genericClient;
    private final HashMap<String, BitcoinJSONRPCClient> walletClients = new HashMap<>();

    public MultiWalletJsonRpcApi(Ledger ledger, NetworkInfo network) {
        this.ledger = ledger;
        this.network = network;
    }

    private void init() {
        if (genericClient != null) {
            return;
        }

        genericClient = new BitcoinJSONRPCClient(network.getUrl().url());
        for (var w : listNames()) {
            walletClients.put(w, createClient(network, w));
        }
    }

    private static BitcoinJSONRPCClient createClient(NetworkInfo network, String walletName) {
        try {
            return new BitcoinJSONRPCClient(new URL(network.getUrl().url() + "wallet/" + walletName));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> listNames() {
        return (List<String>) genericClient.query("listwallets");
    }

    public List<BitcoindRpcClient.Transaction> listTransactions(String account, int count) {
        init();
        if (walletClients.containsKey(account)) {
            return walletClients.get(account).listTransactions("*", count);
        } else {
            return new ArrayList<>();
        }
    }

    public List<BitcoindRpcClient.Transaction> listByAddress(Wallet wallet, int count) {
        init();
        var c = client(wallet).orElseThrow();
        return c.listTransactions("*", count);
    }

    public List<BitcoindRpcClient.Transaction> listReceivedByAddress(Wallet wallet) {
        init();
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
        init();
        var list = new ArrayList<Wallet>();
        for (var c : walletClients.values()) {
            var labels = (List<String>) c.query("listlabels");
            for (var l : labels) {
                list.addAll(getAddressesByLabel(c, l));
            }
        }
        return list;
    }

    public Optional<BitcoinJSONRPCClient> client(Wallet wallet) {
        init();
        for (var c : walletClients.values()) {
            for (var l : (List<String>) c.query("listlabels")) {
                for (var w : getAddressesByLabel(c, l)) {
                    if (WalletCompare.isSame(w, wallet)) {
                        return Optional.of(c);
                    }
                }
            }
        }
        return Optional.empty();
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
        init();
        return genericClient.getRawTransaction(txId);
    }

    public BitcoindRpcClient.DecodedScript decodeScript(String hex) {
        init();
        return genericClient.decodeScript(hex);
    }

    public boolean validateAddress(String address) {
        init();
        try {
            return genericClient.validateAddress(address).isValid();
        } catch (BitcoinRPCException e) {
            // Could fail due "Parse error".
            return false;
        }
    }

    public Optional<BigDecimal> getBalance(Wallet wallet) {
        init();
        var c = client(wallet);
        return c.isPresent() ? Optional.of(c.orElseThrow().getBalance()) : Optional.empty();
    }

    public List<String> walletNames() {
        init();
        return new ArrayList<>(walletClients.keySet());
    }

    public void importWallet(String walletName, LocalDateTime historicTransactionSince, Wallet wallet) throws ApiException {
        init();
        createWallet(walletName);

        var walletAddress = wallet.getPublicKey();
        var resultGetDescriptor = (LinkedHashMap<String, ?>) genericClient.query("getdescriptorinfo", "addr(%s)".formatted(walletAddress));

        var checksum = resultGetDescriptor.get("checksum");
        // Eg. "importdescriptors '[{"desc": "addr(myMubgMuPBGtkgxKz2SaQrD3YMPdTUbVMU)#ky756quq", "timestamp": "now"}]'"
        var options = "[{\"desc\": \"addr(%s)#%s\", \"timestamp\": %s}]".formatted(walletAddress, checksum, toTimestamp(historicTransactionSince));
        importDescriptors(walletName, options);
    }

    private static Object toTimestamp(LocalDateTime historicTransactionSince) {
        return historicTransactionSince.isAfter(LocalDateTime.now()) ? "\"now\"" : historicTransactionSince.toEpochSecond(ZoneOffset.UTC);
    }

    public void importWallet(String walletName, LocalDateTime historicTransactionSince, Device device) throws ApiException {
        init();
        createWallet(walletName);

        var hwi = Hwi.get();
        hwi.chain(genericClient.getBlockChainInfo().chain());

        ArrayList<KeyPool> keyPool;
        try {
            keyPool = hwi.keypool(device, 0, 1000);
        } catch (HwiException e) {
            throw new ApiException(e.getMessage(), e);
        }

        var arr = new JSONArray();
        for (var kp : keyPool) {
            kp.timestamp(toTimestamp(historicTransactionSince));
            arr.put(KeyPoolJsonSerializer.toJson(kp));
        }
        importDescriptors(walletName, arr.toString());
    }

    private void createWallet(String name) throws ApiException {
        final boolean disable_private_keys = true;
        var result = (LinkedHashMap<String, ?>) genericClient.query("createwallet", name, disable_private_keys);
        if (!result.get("name").equals(name)) {
            throw new ApiException("createwallet failed for %s".formatted(name));
        }
    }

    private void importDescriptors(String walletName, String jsonOptions) throws ApiException {
        var options = JSON.parse(jsonOptions);

        var walletClient = createClient(network, walletName);
        try {
            var resultImportDescriptor = (ArrayList<?>) walletClient.query("importdescriptors", options);
            var resultMap = (LinkedHashMap<String, ?>) resultImportDescriptor.get(0);
            if (!(Boolean) resultMap.get("success")) {
                var error = (LinkedHashMap<String, ?>) resultMap.get("error");
                throw new ApiException("importdescriptors failed for %s (%s :%s)".formatted(walletName, error.get("code"), error.get("message")));
            }
        } finally {
            // If user aborts rescan in bitcoinCore, we're able to fetch already scanned data.
            walletClients.put(walletName, walletClient);
        }
    }

    public BitcoindRpcClient.SmartFeeResult estimateSmartFee(int targetInBlocks) {
        init();
        return genericClient.estimateSmartFee(targetInBlocks);
    }

    public boolean isValidWalletPassPhrase(Wallet wallet) {
        try {
            init();
            client(wallet).orElseThrow().walletPassPhrase(wallet.getSecret(), Duration.ofSeconds(1).toMillis());
            return true;
        } catch (BitcoinRPCException e) {
            log.info(e.getMessage(), e);
            return false;
        }
    }
}
