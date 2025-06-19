package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.cryptoledger.MoneyBag;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletCompare;
import com.radynamics.dallipay.cryptoledger.bitcoin.Ledger;
import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.*;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRPCException;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

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
        var ext = new BitcoinCoreRpcClientExt(genericClient);
        return ext.listWallets();
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
                var t = c.getTransaction(txId);
                // Tx is returned multiple times, if eg a hardware wallet is used in multiple bitcoinCore wallets.
                if (list.stream().noneMatch(o -> o.txId().equals(t.txId()))) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    private ArrayList<String> listReceivedByAddress(BitcoinJSONRPCClient client, Wallet wallet) {
        var ext = new BitcoinCoreRpcClientExt(client);
        return ext.listReceivedByAddress(wallet);
    }

    public ArrayList<Wallet> listWallets() {
        init();
        var list = new ArrayList<Wallet>();
        for (var c : walletClients.values()) {
            var ext = new BitcoinCoreRpcClientExt(c);
            var labels = ext.listLabels();
            for (var l : labels) {
                list.addAll(getAddressesByLabel(c, l));
            }
        }
        return list;
    }

    public Optional<BitcoinJSONRPCClient> client(Wallet wallet) {
        init();
        // Return by alias like eg bitbox02.
        if (walletClients.containsKey(wallet.getPublicKey())) {
            return Optional.of(walletClients.get(wallet.getPublicKey()));
        }

        for (var c : walletClients.values()) {
            var ext = new BitcoinCoreRpcClientExt(c);
            for (var l : ext.listLabels()) {
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
        var ext = new BitcoinCoreRpcClientExt(client);
        var list = new ArrayList<Wallet>();
        for (var address : ext.getAddressesByLabel(label)) {
            list.add(ledger.createWallet(address));
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

    public boolean isValidWallet(String identification) {
        init();
        if (walletClients.containsKey(identification)) {
            return true;
        }
        return validateAddress(identification);
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

    public MoneyBag getBalance(Wallet wallet) {
        init();
        var c = client(wallet);
        Optional<BigDecimal> b = c.isPresent() ? Optional.of(c.orElseThrow().getBalance()) : Optional.empty();

        if (!b.isPresent()) {
            log.info("refreshBalance failed. Unknown wallet %s".formatted(wallet.getPublicKey()));
            return new MoneyBag();
        }
        var balances = new MoneyBag();
        balances.set(Money.of(b.orElseThrow().doubleValue(), new Currency(ledger.getNativeCcySymbol())));
        return balances;
    }

    public MoneyBag getBalance(String account) {
        init();
        if (!walletClients.containsKey(account)) {
            return new MoneyBag();
        }

        var b = walletClients.get(account).getBalance();
        var balances = new MoneyBag();
        balances.set(Money.of(b.doubleValue(), new Currency(ledger.getNativeCcySymbol())));
        return balances;
    }

    public List<String> walletNames() {
        init();
        return new ArrayList<>(walletClients.keySet());
    }

    public boolean walletImported(String walletName) {
        for (var name : walletNames()) {
            if (name.equals(walletName)) {
                return true;
            }
        }
        return false;
    }

    public void importWallet(String walletName, LocalDateTime historicTransactionSince, Wallet wallet) throws ApiException {
        init();
        // Only import if not yet exists (prevent BitcoinRPCException "Database already exists").
        if (!walletImported(walletName)) {
            createWallet(walletName);
        }

        var walletAddress = wallet.getPublicKey();
        var ext = new BitcoinCoreRpcClientExt(genericClient);
        var resultGetDescriptor = ext.getDescriptorInfo("addr(%s)".formatted(walletAddress));

        // Eg. "importdescriptors '[{"desc": "addr(myMubgMuPBGtkgxKz2SaQrD3YMPdTUbVMU)#ky756quq", "timestamp": "now"}]'"
        var options = "[{\"desc\": \"addr(%s)#%s\", \"timestamp\": %s}]".formatted(walletAddress, resultGetDescriptor.checksum(), toTimestamp(historicTransactionSince));
        importDescriptors(walletName, new JSONArray(options));
    }

    private static Object toTimestamp(LocalDateTime historicTransactionSince) {
        return historicTransactionSince.isAfter(LocalDateTime.now()) ? "\"now\"" : historicTransactionSince.toEpochSecond(ZoneOffset.UTC);
    }

    public void importWallet(String walletName, LocalDateTime historicTransactionSince, Device device) throws ApiException {
        init();

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

        // Only import if not yet exists (prevent BitcoinRPCException "Database already exists").
        if (!walletImported(walletName)) {
            createWallet(walletName);
        }
        importDescriptors(walletName, arr);
    }

    private void createWallet(String name) throws ApiException {
        var ext = new BitcoinCoreRpcClientExt(genericClient);
        ext.createWallet(name);
    }

    private void importDescriptors(String walletName, JSONArray jsonOptions) throws ApiException {
        importDescriptors(walletName, jsonOptions, 0);
    }

    private void importDescriptors(String walletName, JSONArray jsonOptions, int tryCounter) throws ApiException {
        var walletClient = createClient(network, walletName);
        try {
            var ext = new BitcoinCoreRpcClientExt(walletClient);
            ext.importDescriptors(jsonOptions.toString());
        } catch (ApiException e) {
            // bitcoind returns following exception despite the range is the same as while calling importdescriptors the first time.
            // "importdescriptors failed (-8 :new range must include current range = [0,1001])"
            var msg = e.getMessage();
            if (tryCounter <= 3 && msg.contains("-8 :new range must include current range")) {
                importDescriptors(walletName, fixDescriptorParams(e, jsonOptions), ++tryCounter);
                return;
            }
            throw e;
        } finally {
            // If user aborts rescan in bitcoinCore, we're able to fetch already scanned data.
            walletClients.put(walletName, walletClient);
        }
    }

    private static JSONArray fixDescriptorParams(ApiException e, JSONArray jsonOptions) {
        // "importdescriptors failed (-8 :new range must include current range = [0,1001])"
        var msg = e.getMessage();

        var indexStart = msg.indexOf("[");
        var indexEnd = msg.lastIndexOf("]");
        // Eg. "[0,1001]" -> "0,1001"
        var rangeText = msg.substring(indexStart + 1, indexEnd);
        List<Integer> values = Arrays.stream(rangeText.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        if (values.size() != 2) {
            log.error("could not parse new range from exception text (%s).".formatted(msg));
        }
        var range = Range.between(values.get(0), values.get(1));
        for (var i = 0; i < jsonOptions.length(); i++) {
            KeyPoolJsonSerializer.putRange(jsonOptions.getJSONObject(i), range);
        }
        return jsonOptions;
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
