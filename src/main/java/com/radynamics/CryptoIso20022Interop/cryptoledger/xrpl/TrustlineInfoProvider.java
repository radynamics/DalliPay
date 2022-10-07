package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;

import java.util.ArrayList;

public class TrustlineInfoProvider implements WalletInfoProvider {
    private final TrustlineCache trustlineCache;

    public TrustlineInfoProvider(TrustlineCache cache) {
        this.trustlineCache = cache;
    }

    @Override
    public WalletInfo[] list(Wallet wallet) throws WalletInfoLookupException {
        var list = new ArrayList<WalletInfo>();

        for (var o : trustlineCache.get(WalletConverter.from(wallet))) {
            var limitText = MoneyFormatter.formatFiat(o.getLimit());
            var ccy = o.getLimit().getCcy();
            var value = String.format("%s (%s), limit: %s", ccy.getCode(), ccy.getIssuer().getPublicKey(), limitText);
            list.add(new WalletInfo(this, "", value, 50));
        }

        return list.toArray(new WalletInfo[0]);
    }

    @Override
    public String getDisplayText() {
        return "Trustlines";
    }
}
