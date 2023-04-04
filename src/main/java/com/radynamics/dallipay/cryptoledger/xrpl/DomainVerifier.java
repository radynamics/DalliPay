package com.radynamics.dallipay.cryptoledger.xrpl;

import com.moandjiezana.toml.Toml;
import com.radynamics.dallipay.cryptoledger.Cache;
import com.radynamics.dallipay.cryptoledger.Wallet;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.HashMap;

public class DomainVerifier {
    final static Logger log = LogManager.getLogger(DomainVerifier.class);
    private final Cache<Boolean> cache = new Cache<>("");
    private final Ledger ledger;

    public DomainVerifier(Ledger ledger) {
        this.ledger = ledger;
    }

    public synchronized boolean isValid(Wallet wallet, String domain) {
        cache.evictOutdated();
        var data = cache.get(wallet);
        if (data != null) {
            return data;
        }

        Toml toml;
        try {
            var scheme = domain.startsWith("https://") ? "" : "https://";
            var url = new URL(String.format("%s%s/.well-known/xrp-ledger.toml", scheme, domain));
            toml = new Toml().read(url.openStream());
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            cache.add(wallet, false);
            return false;
        }

        var accounts = toml.getList("ACCOUNTS");
        if (accounts == null) {
            cache.add(wallet, false);
            return false;
        }

        for (var o : accounts) {
            if (!(o instanceof HashMap)) {
                continue;
            }
            var map = ((HashMap<?, String>) o);
            var networkId = map.get("network");
            var matchesNetwork = StringUtils.isEmpty(networkId) || ledger.getNetwork().matches(networkId);

            var address = map.get("address");
            if (matchesNetwork && address.equals(wallet.getPublicKey())) {
                cache.add(wallet, true);
                return true;
            }
        }

        cache.add(wallet, false);
        return false;
    }
}
