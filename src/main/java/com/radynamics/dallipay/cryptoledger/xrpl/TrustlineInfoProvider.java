package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.generic.walletinfo.InfoType;
import com.radynamics.dallipay.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;
import com.radynamics.dallipay.exchange.CurrencyFormatter;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class TrustlineInfoProvider implements WalletInfoProvider {
    private final TrustlineCache trustlineCache;
    private final WalletInfoAggregator walletInfoAggregator;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public TrustlineInfoProvider(TrustlineCache cache) {
        this.trustlineCache = cache;
        this.walletInfoAggregator = new WalletInfoAggregator(new WalletInfoProvider[]{new StaticWalletInfoProvider(cache.getLedger())});
    }

    @Override
    public WalletInfo[] list(Wallet wallet) throws WalletInfoLookupException {
        var list = new ArrayList<WalletInfo>();

        for (var o : trustlineCache.get(WalletConverter.from(wallet))) {
            var sb = new StringBuilder();
            var ccy = o.getLimit().getCcy();
            sb.append(String.format("%s (%s)", ccy.getCode(), toText(ccy.getIssuer())));
            sb.append(String.format(", %s: %s", res.getString("limit"), MoneyFormatter.formatFiat(o.getLimit())));
            var transferFeeText = CurrencyFormatter.formatTransferFee(ccy);
            if (transferFeeText.length() > 0) {
                sb.append(String.format(", %s: %s", res.getString("transferFee"), transferFeeText));
            }
            list.add(new WalletInfo(this, "", sb.toString(), 50));
        }

        return list.toArray(new WalletInfo[0]);
    }

    private String toText(Wallet wallet) {
        return WalletInfoFormatter.format(wallet, walletInfoAggregator.getNameOrDomain(wallet));
    }

    @Override
    public String getDisplayText() {
        return "Trustlines";
    }

    @Override
    public InfoType[] supportedTypes() {
        return new InfoType[0];
    }
}
