package com.radynamics.dallipay.cryptoledger;

public class NetworkInfoFactory {
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

        return livenet != null ? livenet : other;
    }
}
