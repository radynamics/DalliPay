package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.Cache;

public class TrustlineCache {
    private final Ledger ledger;
    private final Cache<Trustline[]> cache;

    public TrustlineCache(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
        this.cache = new Cache<>(ledger.getNetwork().getUrl().toString());
    }

    public Trustline[] get(Wallet wallet) {
        if (wallet == null) throw new IllegalArgumentException("Parameter 'wallet' cannot be null");
        var item = cache.get(wallet);
        if (item != null) {
            return item;
        }

        cache.add(wallet, ledger.listTrustlines(wallet));
        return get(wallet);
    }

    public void clear() {
        cache.clear();
    }

    public Ledger getLedger() {
        return ledger;
    }
}
