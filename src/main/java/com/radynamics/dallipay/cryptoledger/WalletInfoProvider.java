package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.cryptoledger.generic.walletinfo.InfoType;
import com.radynamics.dallipay.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;

public interface WalletInfoProvider {
    WalletInfo[] list(Wallet wallet) throws WalletInfoLookupException;

    String getDisplayText();

    InfoType[] supportedTypes();
}
