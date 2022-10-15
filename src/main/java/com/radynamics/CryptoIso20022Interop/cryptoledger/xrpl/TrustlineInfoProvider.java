package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.InfoType;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;

import java.text.DecimalFormat;
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
            var transferFeeText = toPercentageText(ccy.getTransferFee());
            var value = String.format("%s (%s), limit: %s, transfer fee: %s", ccy.getCode(), toText(ccy.getIssuer()), limitText, transferFeeText);
            list.add(new WalletInfo(this, "", value, 50));
        }

        return list.toArray(new WalletInfo[0]);
    }

    private static String toPercentageText(Double value) {
        var df = DecimalFormat.getInstance();
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        return df.format(value * 100) + "%";
    }

    private String toText(Wallet wallet) {
        var wi = walletInfoAggregator.getNameOrDomain(wallet);
        return wi == null ? wallet.getPublicKey() : WalletInfoFormatter.format(wi);
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
