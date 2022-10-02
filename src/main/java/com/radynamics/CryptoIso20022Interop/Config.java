package com.radynamics.CryptoIso20022Interop;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Config {
    private final static Logger log = LogManager.getLogger(Config.class);

    private ArrayList<NetworkInfo> networkInfos = new ArrayList<>();

    private Config() {
    }

    public static Config fallback(Ledger ledger) {
        return load(ledger, null);
    }

    public static Config load(Ledger ledger, String path) {
        var c = new Config();
        c.networkInfos = loadFile(ledger, path);

        if (c.networkInfos.size() == 0) {
            c.networkInfos.addAll(List.of(ledger.getDefaultNetworkInfo()));
        }

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

        return NetworkInfo.create(HttpUrl.get(endpoint.getString("url")), endpoint.getString("id"));
    }

    public NetworkInfo[] getNetworkInfos() {
        return networkInfos.toArray(new NetworkInfo[0]);
    }

    public Optional<NetworkInfo> getNetwork(String networkId) {
        for (var n : networkInfos) {
            if (StringUtils.equals(networkId, n.getId())) {
                return Optional.of(n);
            }
        }
        return Optional.empty();
    }

    public NetworkInfo getDefaultNetworkInfo() {
        for (var n : networkInfos) {
            if (n.isLivenet()) {
                return n;
            }
        }
        return networkInfos.get(0);
    }
}
