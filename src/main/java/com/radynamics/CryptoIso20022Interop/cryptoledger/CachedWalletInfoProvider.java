package com.radynamics.CryptoIso20022Interop.cryptoledger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CachedWalletInfoProvider implements WalletInfoProvider {
    final static Logger log = LogManager.getLogger(CachedWalletInfoProvider.class);
    private final Ledger ledger;
    private final WalletInfoProvider[] providers;
    private final HashMap<String, WalletInfo[]> cache = new HashMap<>();

    public CachedWalletInfoProvider(Ledger ledger, WalletInfoProvider[] providers) {
        this.ledger = ledger;
        this.providers = providers;
    }

    @Override
    public WalletInfo[] list(Wallet wallet) {
        var key = String.format("%s_%s", ledger.getNetwork().getType(), wallet.getPublicKey());
        if (cache.containsKey(key)) {
            log.trace(String.format("CACHE hit %s", key));
            return cache.get(key);
        }

        var list = new ArrayList<WalletInfo>();
        for (var p : providers) {
            list.addAll(Arrays.asList(p.list(wallet)));
        }

        cache.put(key, list.toArray(new WalletInfo[0]));
        return cache.get(key);
    }
}
