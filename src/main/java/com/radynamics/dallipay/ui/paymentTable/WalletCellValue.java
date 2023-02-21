package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletInfo;

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
