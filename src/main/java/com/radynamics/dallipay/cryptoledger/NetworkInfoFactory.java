package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.Config;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.ui.ExceptionDialog;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NetworkInfoFactory {
    private final static Logger log = LogManager.getLogger(NetworkInfoFactory.class);

    public static NetworkInfo createDefaultOrNull(LedgerId ledgerId) {
        if (ledgerId == null) {
            return null;
        }

        Ledger ledger = LedgerFactory.create(ledgerId);
        NetworkInfo livenet = null;
        NetworkInfo other = null;
        for (var n : ledger.getDefaultNetworkInfo()) {
            if (n.isLivenet()) {
                livenet = n;
            } else {
                other = n;
            }
        }

        var ni = livenet != null ? livenet : other;
        if (ni != null) {
            return ni;
        }

        // Eg. Bitcoin has no getDefaultNetworkInfo(). Use first available.
        try (var repo = new ConfigRepo()) {
            var networkInfos = repo.getCustomSidechains(ledgerId);
            return Arrays.stream(networkInfos).findFirst().orElse(null);
        } catch (Exception e) {
            LogManager.getLogger(ExceptionDialog.class).error(e);
        }
        return null;
    }

    public static NetworkInfo getOrDefault(Ledger ledger, Config config, String networkId) {
        if (!StringUtils.isEmpty(networkId)) {
            var networkByParam = config.getNetwork(networkId.toLowerCase(Locale.ROOT));
            if (networkByParam.isPresent()) {
                return networkByParam.get();
            }
        }

        HttpUrl lastUsed = null;
        var customSidechains = new NetworkInfo[0];
        try (var repo = new ConfigRepo()) {
            lastUsed = repo.getLastUsedRpcUrl(ledger);
            customSidechains = repo.getCustomSidechains(ledger);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (lastUsed == null) {
            return config.getDefaultNetworkInfo();
        }

        var available = new ArrayList<NetworkInfo>();
        available.addAll(List.of(ledger.getDefaultNetworkInfo()));
        available.addAll(List.of(customSidechains));
        available.addAll(Arrays.asList(config.getNetworkInfos()));
        for (var ni : available) {
            if (ni.getUrl().equals(lastUsed)) {
                return ni;
            }
        }

        var ni = NetworkInfo.create(lastUsed, lastUsed.toString());
        var knownNetworkIds = ledger.networkIds();
        ni.setNetworkId(knownNetworkIds.length == 0 ? null : Integer.parseInt(knownNetworkIds[0].getKey()));
        return ni;
    }
}
