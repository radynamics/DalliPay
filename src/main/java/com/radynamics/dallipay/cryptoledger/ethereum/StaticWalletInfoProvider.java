package com.radynamics.dallipay.cryptoledger.ethereum;

public class StaticWalletInfoProvider extends com.radynamics.dallipay.cryptoledger.generic.walletinfo.StaticWalletInfoProvider {
    public StaticWalletInfoProvider(Ledger ledger) {
        super(ledger);

        add("0xdAC17F958D2ee523a2206206994597C13D831ec7", "Tether"); // USDT
        add("0xB8c77482e45F1F44dE1745F52C74426C631bDD52", "Binance"); // BNB
        add("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", "Circle"); // USDC
        add("0xb4272071ecadd69d933adcd19ca99fe80664fc08", "Bitcoin Suisse AG"); // XCHF
    }
}
