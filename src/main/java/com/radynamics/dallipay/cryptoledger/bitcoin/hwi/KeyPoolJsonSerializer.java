package com.radynamics.dallipay.cryptoledger.bitcoin.hwi;

import org.apache.commons.lang3.Range;
import org.json.JSONArray;
import org.json.JSONObject;

public class KeyPoolJsonSerializer {
    public static KeyPool parse(JSONObject json) {
        var o = new KeyPool();
        o.desc(json.getString("desc"));
        var r = json.getJSONArray("range");
        o.range(Range.between(r.getInt(0), r.getInt(1)));
        o.timestamp(json.get("timestamp"));
        o.internal(json.getBoolean("internal"));
        o.keypool(json.getBoolean("keypool"));
        o.active(json.getBoolean("active"));
        o.watchonly(json.getBoolean("watchonly"));
        return o;
    }

    public static JSONObject toJson(KeyPool keyPool) {
        var o = new JSONObject();
        o.put("desc", keyPool.desc());
        var r = new JSONArray();
        r.put(keyPool.range().getMinimum());
        r.put(keyPool.range().getMaximum());
        o.put("range", r);
        o.put("timestamp", keyPool.timestamp());
        o.put("internal", keyPool.internal());
        o.put("keypool", keyPool.keypool());
        o.put("active", keyPool.active());
        o.put("watchonly", keyPool.watchonly());
        return o;
    }
}
