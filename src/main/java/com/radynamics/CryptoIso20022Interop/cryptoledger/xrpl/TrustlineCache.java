package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletCompare;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api.JsonRpcApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class TrustlineCache {
    private final static Logger log = LogManager.getLogger(TrustlineCache.class);
    private final JsonRpcApi api;
    private final HashMap<Wallet, Trustline[]> items = new HashMap<>();

    public TrustlineCache(JsonRpcApi api) {
        if (api == null) throw new IllegalArgumentException("Parameter 'api' cannot be null");
        this.api = api;
    }

    public Trustline[] get(Wallet wallet) {
        if (wallet == null) throw new IllegalArgumentException("Parameter 'wallet' cannot be null");
        var item = find(wallet);
        if (item != null) {
            return item;
        }

        load(wallet);
        return get(wallet);
    }

    private Trustline[] find(Wallet wallet) {
        for (var item : items.entrySet()) {
            if (WalletCompare.isSame(item.getKey(), wallet)) {
                log.trace(String.format("CACHE hit %s (%s trustlines)", item.getKey().getPublicKey(), item.getValue().length));
                return item.getValue();
            }
        }
        return null;
    }

    private void load(Wallet wallet) {
        items.put(wallet, api.listTrustlines(wallet));
    }

    public void clear() {
        items.clear();
    }
}
