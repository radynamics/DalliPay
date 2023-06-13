package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.Cache;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.WalletKey;
import com.radynamics.dallipay.cryptoledger.generic.Wallet;

public class TrustlineCache {
    private final Ledger ledger;
    private final Cache<Trustline[]> cache;

    public TrustlineCache(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
        this.cache = new Cache<>(createPrefix(ledger.getNetwork()));
    }

    private static String createPrefix(NetworkInfo network) {
        return network.getUrl().toString();
    }

    public Trustline[] get(Wallet wallet) {
        if (wallet == null) throw new IllegalArgumentException("Parameter 'wallet' cannot be null");
        var key = new WalletKey(wallet);
        var item = cache.get(key);
        if (item != null) {
            return item;
        }

        cache.add(key, ledger.listTrustlines(wallet));
        return get(wallet);
    }

    public void clear() {
        cache.clear();
    }

    public Ledger getLedger() {
        return ledger;
    }

    public boolean networkChanged(NetworkInfo network) {
        return !cache.getPrefix().equals(createPrefix(network));
    }
}
