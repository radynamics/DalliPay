package com.radynamics.dallipay.cryptoledger.generic.walletinfo;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletInfo;
import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;

import java.util.HashMap;
import java.util.ResourceBundle;

public abstract class StaticWalletInfoProvider implements WalletInfoProvider {
    private final Ledger ledger;

    private final HashMap<String, WalletInfo[]> known = new HashMap<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    protected StaticWalletInfoProvider(Ledger ledger) {
        this.ledger = ledger;
    }

    protected void add(String walletPublicKey, String name) {
        known.put(walletPublicKey, createWalletInfos(name));
    }

    private WalletInfo[] createWalletInfos(String name) {
        return new WalletInfo[]{new WalletInfo(this, name, InfoType.Name)};
    }

    @Override
    public WalletInfo[] list(Wallet wallet) {
        var key = wallet.getPublicKey();
        return !ledger.getNetwork().isLivenet() || !known.containsKey(key) ? new WalletInfo[0] : known.get(key);
    }

    @Override
    public String getDisplayText() {
        return res.getString("staticinformation");
    }

    @Override
    public InfoType[] supportedTypes() {
        return new InfoType[]{InfoType.Name};
    }
}
