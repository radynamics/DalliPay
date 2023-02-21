package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletInfo;
import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;
import com.radynamics.dallipay.cryptoledger.xrpl.walletinfo.InfoType;

import java.util.HashMap;

public class StaticWalletInfoProvider implements WalletInfoProvider {
    private final Ledger ledger;

    private final HashMap<String, WalletInfo[]> known = new HashMap<>();

    public StaticWalletInfoProvider(Ledger ledger) {
        this.ledger = ledger;

        known.put("rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq", createWalletInfos("GateHub"));
        known.put("rhotcWYdfn6qxhVMbPKGDF3XCKqwXar5J4", createWalletInfos("GateHub"));
        known.put("rctArjqVvTHihekzDeecKo6mkTYTUSBNc", createWalletInfos("GateHub Fifth"));
        known.put("rchGBxcD1A1C2tdxF6papQYZ8kjRKMYcL", createWalletInfos("GateHub Fifth"));
        known.put("rcA8X3TVMST1n3CJeAdGk1RdRCHii7N2h", createWalletInfos("GateHub Fifth"));
        known.put("rcRzGWq6Ng3jeYhqnmM4zcWcUh69hrQ8V", createWalletInfos("GateHub Fifth"));
        known.put("rcEGREd8NmkKRE8GE424sksyt1tJVFZwu", createWalletInfos("GateHub USDC"));
        known.put("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B", createWalletInfos("Bitstamp"));
        known.put("rsoLo2S1kiGeCcn6hCUXVrCpGMWLrRrLZz", createWalletInfos("Sologenic"));
        known.put("rs5hzzF7RzdV2Ub7Fv14Z1ShjuCrnL7FEX", createWalletInfos("STASIS"));
        known.put("rEaC7oNDB34K6CiSbSrA6xEPeVhXNgDZRY", createWalletInfos("Xago"));
        known.put("rEn9eRkX25wfGPLysUMAvZ84jAzFNpT5fL", createWalletInfos("Stably"));
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

    @Override
    public InfoType[] supportedTypes() {
        return new InfoType[]{InfoType.Name};
    }
}
