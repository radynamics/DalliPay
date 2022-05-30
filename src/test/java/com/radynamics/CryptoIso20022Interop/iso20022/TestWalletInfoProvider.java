package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestWalletInfoProvider implements WalletInfoProvider {
    private final Map<String, ArrayList<WalletInfo>> list = new HashMap<>();

    public void add(String walletPublicKey, WalletInfo info) {
        if (!list.containsKey(walletPublicKey)) {
            list.put(walletPublicKey, new ArrayList<>());
        }
        list.get(walletPublicKey).add(info);
    }

    @Override
    public WalletInfo[] list(Wallet wallet) {
        for (var entry : list.entrySet()) {
            if (entry.getKey().equals(wallet.getPublicKey())) {
                return entry.getValue().toArray(new WalletInfo[0]);
            }
        }
        return new WalletInfo[0];
    }
}
