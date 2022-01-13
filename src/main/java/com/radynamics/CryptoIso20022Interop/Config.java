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
    private NetworkInfo networkInfo;

    private Config() {
    }

    public static Config load(Ledger ledger, Network type, String path) {
        var c = new Config();
        c.setNetworkInfo(new NetworkInfo(type, ledger.getFallbackUrl(type)));

        var file = new File(path);
        if (!file.exists()) {
            return c;
        }

        JSONObject json;
        try {
            var bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            json = new JSONObject(new JSONTokener(bufferedReader));
        } catch (FileNotFoundException e) {
            LogManager.getLogger().error(String.format("Reading config file %s failed. Taking default values instead.", path), e);
            return c;
        }

        if (!json.has(ledger.getId())) {
            return c;
        }

        var n = json.getJSONObject(ledger.getId());
        var networkInfo = getNetworkInfoOrNull(n, type);
        if (networkInfo != null) {
            c.setNetworkInfo(networkInfo);
        }
        return c;
    }

    public static NetworkInfo getNetworkInfoOrNull(JSONObject json, Network type) {
        if (type == Network.Live && json.has("liveUrl")) {
            return new NetworkInfo(type, HttpUrl.get(json.getString("liveUrl")));
        }
        if (type == Network.Test && json.has("testUrl")) {
            return new NetworkInfo(type, HttpUrl.get(json.getString("testUrl")));
        }

        return null;
    }

    private void setNetworkInfo(NetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
    }

    public NetworkInfo getNetworkInfo() {
        return networkInfo;
    }
}
