package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletInfo;

public class WalletCellValue {
    private final Wallet wallet;
    private final String destinationTag;
    private final WalletInfo[] walletInfos;

    public WalletCellValue(Wallet wallet, String destinationTag) {
        this(wallet, destinationTag, new WalletInfo[0]);
    }

    public WalletCellValue(Wallet wallet, String destinationTag, WalletInfo[] walletInfos) {
        this.wallet = wallet;
        this.destinationTag = destinationTag;
        this.walletInfos = walletInfos;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getDestinationTag() {
        return destinationTag;
    }

    public WalletInfo[] getWalletInfos() {
        return walletInfos;
    }
}
