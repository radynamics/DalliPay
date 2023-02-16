package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfo;

public class WalletCellValue {
    private final Wallet wallet;
    private final WalletInfo[] walletInfos;

    public WalletCellValue(Wallet wallet) {
        this(wallet, new WalletInfo[0]);
    }

    public WalletCellValue(Wallet wallet, WalletInfo[] walletInfos) {
        this.wallet = wallet;
        this.walletInfos = walletInfos;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public WalletInfo[] getWalletInfos() {
        return walletInfos;
    }
}
