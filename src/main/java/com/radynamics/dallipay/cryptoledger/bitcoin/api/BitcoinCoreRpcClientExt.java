package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.krotjson.HexCoder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BitcoinCoreRpcClientExt {
    private final BitcoinJSONRPCClient client;

    public BitcoinCoreRpcClientExt(BitcoinJSONRPCClient client) {
        this.client = client;
    }

    public WalletCreateFundedPsbtResult walletCreateFundedPsbt(List<BitcoindRpcClient.TxOutput> txOutputs) {
        var inputs = new ArrayList<Map<String, ?>>();
        var outputs = new LinkedHashMap<String, Object>();
        for (var txOutput : txOutputs) {
            outputs.put(txOutput.address(), txOutput.amount());
            if (txOutput.data() != null) {
                String hex = HexCoder.encode(txOutput.data());
                outputs.put("data", hex);
            }
        }

        var result = (LinkedHashMap<String, ?>) client.query("walletcreatefundedpsbt", inputs, outputs);
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
}
