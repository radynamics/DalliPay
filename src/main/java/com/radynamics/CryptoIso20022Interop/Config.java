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
import java.util.ArrayList;

public class Config {
    private final static Logger log = LogManager.getLogger(Config.class);

    private NetworkInfo live;
    private NetworkInfo test;

    private Config() {
    }

    public static Config fallback(Ledger ledger) {
        return load(ledger, null);
    }

    public static Config load(Ledger ledger, String path) {
        var c = new Config();
        var loaded = loadFile(ledger, path);
        for (var n : loaded) {
            if (NetworkInfo.liveId.equals(n.getId())) {
                c.live = n;
            }
            if (NetworkInfo.testnetId.equals(n.getId())) {
                c.test = n;
            }
        }

        c.live = c.live != null ? c.live : new NetworkInfo(ledger.getFallbackUrl(Network.Live), NetworkInfo.liveId);
        c.test = c.test != null ? c.test : new NetworkInfo(ledger.getFallbackUrl(Network.Test), NetworkInfo.testnetId);

        return c;
    }

    private static ArrayList<NetworkInfo> loadFile(Ledger ledger, String path) {
        var list = new ArrayList<NetworkInfo>();

        if (path == null || !new File(path).exists()) {
            return list;
        }

        JSONObject json;
        try {
            var bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            json = new JSONObject(new JSONTokener(bufferedReader));
        } catch (FileNotFoundException e) {
            log.error(String.format("Reading config file %s failed. Taking default values instead.", path), e);
            return list;
        }

        if (!json.has(ledger.getId().textId())) {
            return list;
        }

        var n = json.getJSONObject(ledger.getId().textId());
        if (!n.has("rpcEndpoints")) {
            return list;
        }

        var endpoints = n.getJSONArray("rpcEndpoints");
        for (var i = 0; i < endpoints.length(); i++) {
            var e = endpoints.getJSONObject(i);
            var info = getNetworkInfoOrNull(e);
            if (info != null) {
                list.add(info);
            }
        }
        return list;
    }

    private static NetworkInfo getNetworkInfoOrNull(JSONObject endpoint) {
        if (!endpoint.has("id") || !endpoint.has("url")) {
            return null;
        }

        return new NetworkInfo(HttpUrl.get(endpoint.getString("url")), endpoint.getString("id"));
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
