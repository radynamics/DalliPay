package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.google.common.primitives.UnsignedLong;
import com.radynamics.dallipay.cryptoledger.Wallet;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRPCException;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.krotjson.HexCoder;
import wf.bitcoin.krotjson.JSON;

import java.util.*;

public class BitcoinCoreRpcClientExt {
    private final BitcoinJSONRPCClient client;

    public BitcoinCoreRpcClientExt(BitcoinJSONRPCClient client) {
        this.client = client;
    }

    public WalletCreateFundedPsbtResult walletCreateFundedPsbt(List<BitcoindRpcClient.TxOutput> txOutputs, UnsignedLong feeSatsPerByte) {
        var inputs = new ArrayList<Map<String, ?>>();
        var outputs = new LinkedHashMap<String, Object>();
        for (var txOutput : txOutputs) {
            outputs.put(txOutput.address(), txOutput.amount());
            if (txOutput.data() != null) {
                String hex = HexCoder.encode(txOutput.data());
                outputs.put("data", hex);
            }
        }

        final var locktime = 0;
        var options = new LinkedHashMap<String, Object>();
        options.put("fee_rate", feeSatsPerByte);

        var result = (LinkedHashMap<String, ?>) client.query("walletcreatefundedpsbt", inputs, outputs, locktime, options);
        return new WalletCreateFundedPsbtResult(result.get("psbt").toString(), Double.parseDouble(result.get("fee").toString()), Integer.parseInt(result.get("changepos").toString()));
    }

    public WalletProcessPsbtResult walletProcessPsbt(String psbt) {
        var result = (LinkedHashMap<String, ?>) client.query("walletprocesspsbt", psbt);
        return new WalletProcessPsbtResult(result.get("psbt").toString(), Boolean.parseBoolean(result.get("complete").toString()), false);
    }

    public FinalizePsbtResult finalizePsbt(WalletProcessPsbtResult signedPsbt) {
        var result = (LinkedHashMap<String, ?>) client.query("finalizepsbt", signedPsbt.psbt());
        return new FinalizePsbtResult(
                result.get("hex").toString(),
                Boolean.parseBoolean(result.get("complete").toString())
        );
    }

    public void createWallet(String name) throws ApiException {
        final boolean disable_private_keys = true;
        final boolean blank = false;
        final String passphrase = null;
        final boolean avoid_reuse = false;
        final Boolean descriptors = null;
        // Necessary to remain accessible via rpc after Bitcoin Core restart.
        final boolean load_on_startup = true;
        var result = (LinkedHashMap<String, ?>) client.query("createwallet", name, disable_private_keys, blank, passphrase, avoid_reuse, descriptors, load_on_startup);
        if (!result.get("name").equals(name)) {
            throw new ApiException("createwallet failed for %s".formatted(name));
        }
    }

    public void importDescriptors(String jsonOptions) throws ApiException {
        var resultImportDescriptor = (ArrayList<?>) client.query("importdescriptors", JSON.parse(jsonOptions));
        var resultMap = (LinkedHashMap<String, ?>) resultImportDescriptor.get(0);
        if (!(Boolean) resultMap.get("success")) {
            var error = (LinkedHashMap<String, ?>) resultMap.get("error");
            throw new ApiException("importdescriptors failed (%s :%s)".formatted(error.get("code"), error.get("message")));
        }
    }

    public List<String> listWallets() {
        return (List<String>) client.query("listwallets");
    }

    public ArrayList<String> listReceivedByAddress(Wallet wallet) {
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

    public List<String> listLabels() {
        return (List<String>) client.query("listlabels");
    }

    public List<String> getAddressesByLabel(String label) {
        var result = (LinkedHashMap<String, String>) client.query("getaddressesbylabel", label);
        var list = new ArrayList<String>();
        for (var kvp : result.entrySet()) {
            list.add(kvp.getKey());
        }
        return list;
    }

    public DescriptorInfoResult getDescriptorInfo(String desc) {
        var result = (LinkedHashMap<String, ?>) client.query("getdescriptorinfo", desc);
        return new DescriptorInfoResult(String.valueOf(result.get("checksum")));
    }

    public static Optional<JSONObject> errorJson(Throwable t) {
        if (!(t instanceof BitcoinRPCException)) {
            return Optional.empty();
        }
        final var rpcEx = (BitcoinRPCException) t;
        if (StringUtils.isEmpty(rpcEx.getResponse())) {
            return Optional.empty();
        }
        return Optional.of(new JSONObject(rpcEx.getResponse()).getJSONObject("error"));
    }
}
