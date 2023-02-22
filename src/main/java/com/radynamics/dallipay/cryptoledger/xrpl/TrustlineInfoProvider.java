package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.xrpl.walletinfo.InfoType;
import com.radynamics.dallipay.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;
import com.radynamics.dallipay.exchange.CurrencyFormatter;

import java.util.ArrayList;

public class TrustlineInfoProvider implements WalletInfoProvider {
    private final TrustlineCache trustlineCache;
    private final WalletInfoAggregator walletInfoAggregator;

    public TrustlineInfoProvider(TrustlineCache cache) {
        this.trustlineCache = cache;
        this.walletInfoAggregator = new WalletInfoAggregator(new WalletInfoProvider[]{new StaticWalletInfoProvider(cache.getLedger())});
    }

    @Override
    public WalletInfo[] list(Wallet wallet) throws WalletInfoLookupException {
        var list = new ArrayList<WalletInfo>();

        for (var o : trustlineCache.get(WalletConverter.from(wallet))) {
            var limitText = MoneyFormatter.formatFiat(o.getLimit());
            var ccy = o.getLimit().getCcy();
            var transferFeeText = CurrencyFormatter.formatTransferFee(ccy);
            var value = String.format("%s (%s), limit: %s, transfer fee: %s", ccy.getCode(), toText(ccy.getIssuer()), limitText, transferFeeText);
            list.add(new WalletInfo(this, "", value, 50));
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