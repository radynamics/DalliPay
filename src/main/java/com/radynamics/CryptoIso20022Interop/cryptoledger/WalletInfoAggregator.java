package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.InfoType;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class WalletInfoAggregator {
    final static Logger log = LogManager.getLogger(WalletInfoAggregator.class);

    private WalletInfoProvider[] providers;

    public WalletInfoAggregator(WalletInfoProvider[] providers) {
        this.providers = providers;
    }

    public WalletInfo getNameOrDomain(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        var list = new ArrayList<WalletInfo>();
        for (var p : providers) {
            try {
                list.addAll(Arrays.asList(p.list(wallet)));
            } catch (WalletInfoLookupException e) {
                log.warn(e);
            }
        }

        WalletInfo name = null;
        WalletInfo domain = null;
        for (var wi : list) {
            if (wi.getType() == InfoType.Name) {
                name = wi;
            }
            if (wi.getType() == InfoType.Domain) {
                domain = wi;
            }
        }

        return name != null ? name : domain;
    }
}
