package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.WalletInfoLookupException;

public interface WalletInfoProvider {
    WalletInfo[] list(Wallet wallet) throws WalletInfoLookupException;
}
