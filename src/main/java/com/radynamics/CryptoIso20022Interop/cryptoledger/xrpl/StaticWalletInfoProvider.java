package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoProvider;

import java.util.HashMap;

public class StaticWalletInfoProvider implements WalletInfoProvider {
    private final Ledger ledger;

    private final HashMap<String, WalletInfo[]> known = new HashMap<>();

    public StaticWalletInfoProvider(Ledger ledger) {
        this.ledger = ledger;

        known.put("rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq", new WalletInfo[]{new WalletInfo("Name", "GateHub", 100)});
        known.put("rhotcWYdfn6qxhVMbPKGDF3XCKqwXar5J4", new WalletInfo[]{new WalletInfo("Name", "GateHub", 100)});
        known.put("rctArjqVvTHihekzDeecKo6mkTYTUSBNc", new WalletInfo[]{new WalletInfo("Name", "GateHub Fifth", 100)});
        known.put("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B", new WalletInfo[]{new WalletInfo("Name", "Bitstamp", 100)});
        known.put("rsoLo2S1kiGeCcn6hCUXVrCpGMWLrRrLZz", new WalletInfo[]{new WalletInfo("Name", "Sologenic", 100)});
    }

    @Override
    public WalletInfo[] list(Wallet wallet) {
        var key = wallet.getPublicKey();
        return ledger.getNetwork().getType() != Network.Live || !known.containsKey(key) ? new WalletInfo[0] : known.get(key);
    }
}
