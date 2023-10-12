package com.radynamics.dallipay.cryptoledger;

import okhttp3.HttpUrl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;

public final class NetworkInfoJsonSerializer {
    public static JSONArray toJsonArray(NetworkInfo[] entries) {
        if (entries == null) {
            return new JSONArray();
        }

        var a = new JSONArray();
        for (var e : entries) {
            var json = new JSONObject();
            a.put(json);
            json.put("displayName", e.getDisplayName());
            json.put("rpcUrl", e.getUrl());
            json.put("websocketUrl", e.getWebSocketUri() == null ? null : e.getWebSocketUri().toString());
        }

        return a;
    }

    public static NetworkInfo[] parse(JSONArray json) {
        var list = new ArrayList<NetworkInfo>();
        for (var i = 0; i < json.length(); i++) {
            var e = json.getJSONObject(i);
            var ni = NetworkInfo.create(HttpUrl.get(e.getString("rpcUrl")), e.getString("displayName"));
            var websocketUrl = e.optString("websocketUrl", "");
            if (websocketUrl.length() > 0) {
                ni.setWebSocketUri(URI.create(websocketUrl));
            }
            list.add(ni);
        }
        return list.toArray(NetworkInfo[]::new);
    }
}
