package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.ui.ExceptionDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

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
}
