package com.radynamics.CryptoIso20022Interop.cryptoledger;

import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;

public final class NetworkHelper {
    private static HashMap<Network, String> map;

    static {
        map = new HashMap<>();
        map.put(Network.Test, "TEST");
        map.put(Network.Live, "LIVE");
    }

    public static String toShort(Network value) {
        for (var o : map.entrySet()) {
            if (o.getKey() == value) {
                return o.getValue();
            }
        }
        throw new NotImplementedException(String.format("Value %s unknown.", value));
    }
}
