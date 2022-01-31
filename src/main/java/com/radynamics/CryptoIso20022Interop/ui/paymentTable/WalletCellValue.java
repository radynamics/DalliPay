package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfo;

public class WalletCellValue {
    private Wallet wallet;
    private WalletInfo walletInfo;

    public WalletCellValue(Wallet wallet) {
        this(wallet, null);
    }

    public WalletCellValue(Wallet wallet, WalletInfo walletInfo) {
        this.wallet = wallet;
        this.walletInfo = walletInfo;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public WalletInfo getWalletInfo() {
        return walletInfo;
    }
}
