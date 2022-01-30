package com.radynamics.CryptoIso20022Interop;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

public class Config {
    private NetworkInfo live;
    private NetworkInfo test;

    private Config() {
    }

    public static Config load(Ledger ledger, String path) {
        var c = new Config();
        c.live = load(ledger, path, Network.Live);
        c.test = load(ledger, path, Network.Test);
        return c;
    }

    private static NetworkInfo load(Ledger ledger, String path, Network type) {
        var fallback = new NetworkInfo(type, ledger.getFallbackUrl(type));

        var file = new File(path);
        if (!file.exists()) {
            return fallback;
        }

        JSONObject json;
        try {
            var bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            json = new JSONObject(new JSONTokener(bufferedReader));
        } catch (FileNotFoundException e) {
            LogManager.getLogger().error(String.format("Reading config file %s failed. Taking default values instead.", path), e);
            return fallback;
        }

        if (!json.has(ledger.getId())) {
            return fallback;
        }

        var n = json.getJSONObject(ledger.getId());
        var info = getNetworkInfoOrNull(n, type);
        return info == null ? fallback : info;
    }

    private static NetworkInfo getNetworkInfoOrNull(JSONObject json, Network type) {
        if (type == Network.Live && json.has("liveUrl")) {
            return new NetworkInfo(type, HttpUrl.get(json.getString("liveUrl")));
        }
        if (type == Network.Test && json.has("testUrl")) {
            return new NetworkInfo(type, HttpUrl.get(json.getString("testUrl")));
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
