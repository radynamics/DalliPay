package com.radynamics.CryptoIso20022Interop.cryptoledger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class WalletInfoAggregator {
    private WalletInfoProvider[] providers;

    public WalletInfoAggregator(WalletInfoProvider[] providers) {
        this.providers = providers;
    }

    public WalletInfo getMostImportant(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        var list = new ArrayList<WalletInfo>();
        for (var p : providers) {
            list.addAll(Arrays.asList(p.list(wallet)));
        }
        return list.stream().sorted(Comparator.comparing(WalletInfo::getImportance)).findFirst().orElse(null);
    }
}
