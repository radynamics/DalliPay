package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.InfoType;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class WalletInfoAggregator {
    final static Logger log = LogManager.getLogger(WalletInfoAggregator.class);

    private WalletInfoProvider[] providers;

    public WalletInfoAggregator(WalletInfoProvider[] providers) {
        this.providers = providers;
    }

    public WalletInfo[] all(Wallet wallet) {
        if (wallet == null) {
            return new WalletInfo[0];
        }

        var list = new ArrayList<WalletInfo>();
        for (var p : providers) {
            try {
                list.addAll(Arrays.asList(p.list(wallet)));
            } catch (WalletInfoLookupException e) {
                log.warn(e);
            }
        }
        return list.toArray(new WalletInfo[0]);
    }

    public WalletInfo getNameOrDomain(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        var list = new ArrayList<WalletInfo>();
        var filtered = filterBy(new InfoType[]{InfoType.Name, InfoType.Domain});
        for (var p : filtered) {
            try {
                list.addAll(Arrays.asList(p.list(wallet)));
            } catch (WalletInfoLookupException e) {
                log.warn(e);
            }
        }

        return getNameOrDomain(list.toArray(new WalletInfo[0]));
    }

    public static WalletInfo getNameOrDomain(WalletInfo[] walletInfos) {
        var names = Arrays.stream(walletInfos).filter(o -> o.getType() == InfoType.Name);
        var domains = Arrays.stream(walletInfos).filter(o -> o.getType() == InfoType.Domain);

        var name = names.min(Comparator.comparing(WalletInfo::getImportance));
        var domain = domains.min(Comparator.comparing(WalletInfo::getImportance));
        return name.orElse(domain.orElse(null));
    }

    private WalletInfoProvider[] filterBy(InfoType[] infoTypes) {
        var set = new HashSet<WalletInfoProvider>();
        for (var p : providers) {
            if (Arrays.stream(infoTypes).anyMatch(it -> ArrayUtils.contains(p.supportedTypes(), it))) {
                set.add(p);
            }
        }
        return set.toArray(new WalletInfoProvider[0]);
    }
}
