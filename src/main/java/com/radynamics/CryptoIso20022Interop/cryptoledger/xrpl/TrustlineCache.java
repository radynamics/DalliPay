package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class TrustlineCache {
    private final static Logger log = LogManager.getLogger(TrustlineCache.class);
    private final Ledger ledger;
    private final HashMap<String, Trustline[]> items = new HashMap<>();

    public TrustlineCache(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
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
        var key = createKey(wallet);
        for (var item : items.entrySet()) {
            if (key.equals(item.getKey())) {
                log.trace(String.format("CACHE hit %s (%s trustlines)", key, item.getValue().length));
                return item.getValue();
            }
        }
        return null;
    }

    private String createKey(Wallet wallet) {
        return String.format("%s_%s_%s", ledger.getId().textId(), ledger.getNetwork().getUrl(), wallet.getPublicKey());
    }

    private void load(Wallet wallet) {
        items.put(createKey(wallet), ledger.listTrustlines(wallet));
    }

    public void clear() {
        items.clear();
    }
}
