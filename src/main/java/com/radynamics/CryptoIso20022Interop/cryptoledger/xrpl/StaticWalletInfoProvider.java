package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.InfoType;

import java.util.HashMap;

public class StaticWalletInfoProvider implements WalletInfoProvider {
    private final Ledger ledger;

    private final HashMap<String, WalletInfo[]> known = new HashMap<>();

    public StaticWalletInfoProvider(Ledger ledger) {
        this.ledger = ledger;

        known.put("rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq", createWalletInfos("GateHub"));
        known.put("rhotcWYdfn6qxhVMbPKGDF3XCKqwXar5J4", createWalletInfos("GateHub"));
        known.put("rctArjqVvTHihekzDeecKo6mkTYTUSBNc", createWalletInfos("GateHub Fifth"));
        known.put("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B", createWalletInfos("Bitstamp"));
        known.put("rsoLo2S1kiGeCcn6hCUXVrCpGMWLrRrLZz", createWalletInfos("Sologenic"));
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
        return "Static information";
    }
}
