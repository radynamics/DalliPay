package com.radynamics.CryptoIso20022Interop;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

public class Config {
    final static Logger log = LogManager.getLogger(Config.class);

    private NetworkInfo live;
    private NetworkInfo test;

    private Config() {
    }

    public static Config fallback(Ledger ledger) {
        return load(ledger, null);
    }

    public static Config load(Ledger ledger, String path) {
        var c = new Config();
        c.live = load(ledger, path, Network.Live);
        c.test = load(ledger, path, Network.Test);
        return c;
    }

    private static NetworkInfo load(Ledger ledger, String path, Network type) {
        var fallback = new NetworkInfo(type, ledger.getFallbackUrl(type));

        if (path == null || !new File(path).exists()) {
            return fallback;
        }

        JSONObject json;
        try {
            var bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            json = new JSONObject(new JSONTokener(bufferedReader));
        } catch (FileNotFoundException e) {
            log.error(String.format("Reading config file %s failed. Taking default values instead.", path), e);
            return fallback;
        }

        if (!json.has(ledger.getId().textId())) {
            return fallback;
        }

        var n = json.getJSONObject(ledger.getId().textId());
        if (!n.has("rpcEndpoints")) {
            return fallback;
        }

        var endpoints = n.getJSONArray("rpcEndpoints");
        for (var i = 0; i < endpoints.length(); i++) {
            var e = endpoints.getJSONObject(i);
            var info = getNetworkInfoOrNull(e, type);
            if (info != null) {
                return info;
            }
        }
        return fallback;
    }

    private static NetworkInfo getNetworkInfoOrNull(JSONObject endpoint, Network type) {
        if (!endpoint.has("id") || !endpoint.has("url")) {
            return null;
        }

        var url = HttpUrl.get(endpoint.getString("url"));
        if (type == Network.Live && "livenet".equals(endpoint.getString("id"))) {
            return new NetworkInfo(type, url);
        }
        if (type == Network.Test && "testnet".equals(endpoint.getString("id"))) {
            return new NetworkInfo(type, url);
        }

        return null;
    }

    public NetworkInfo getNetwork(Network network) {
        switch (network) {
            case Live -> {
                return live;
            }
            case Test -> {
                return test;
            }
            default -> throw new IllegalStateException("Unexpected value: " + network);
        }
    }
}
