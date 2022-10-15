package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.InfoType;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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
    public WalletInfo[] list(Wallet wallet) throws WalletInfoLookupException {
        var key = String.format("%s_%s", ledger.getNetwork().getId(), wallet.getPublicKey());
        if (cache.containsKey(key)) {
            log.trace(String.format("CACHE hit %s", key));
            return cache.get(key);
        }

        var list = new ArrayList<WalletInfo>();
        for (var p : providers) {
            try {
                list.addAll(Arrays.asList(p.list(wallet)));
            } catch (WalletInfoLookupException e) {
                log.warn(e.getMessage(), e);
            }
        }

        cache.put(key, list.toArray(new WalletInfo[0]));
        return cache.get(key);
    }

    @Override
    public String getDisplayText() {
        return "Cached values";
    }

    @Override
    public InfoType[] supportedTypes() {
        var set = new HashSet<InfoType>();
        for (var p : providers) {
            set.addAll(List.of(p.supportedTypes()));
        }
        return set.toArray(new InfoType[0]);
    }
}
