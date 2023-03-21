package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletInfo;
import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;
import com.radynamics.dallipay.cryptoledger.xrpl.walletinfo.InfoType;

import java.util.HashMap;
import java.util.ResourceBundle;

public class StaticWalletInfoProvider implements WalletInfoProvider {
    private final Ledger ledger;

    private final HashMap<String, WalletInfo[]> known = new HashMap<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public StaticWalletInfoProvider(Ledger ledger) {
        this.ledger = ledger;

        known.put("rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq", createWalletInfos("GateHub")); // USD, EUR
        known.put("rhotcWYdfn6qxhVMbPKGDF3XCKqwXar5J4", createWalletInfos("GateHub")); // USD, EUR
        known.put("r4GN9eEoz9K4BhMQXe4H1eYNtvtkwGdt8g", createWalletInfos("GateHub")); // GBP
        known.put("rctArjqVvTHihekzDeecKo6mkTYTUSBNc", createWalletInfos("GateHub")); // SGB
        known.put("rchGBxcD1A1C2tdxF6papQYZ8kjRKMYcL", createWalletInfos("GateHub")); // BTC
        known.put("rcyS4CeCZVYvTiKcxj6Sx32ibKwcDHLds", createWalletInfos("GateHub")); // BCH
        known.put("rcA8X3TVMST1n3CJeAdGk1RdRCHii7N2h", createWalletInfos("GateHub")); // ETH
        known.put("rDAN8tzydyNfnNf2bfUQY6iR96UbpvNsze", createWalletInfos("GateHub")); // ETC
        known.put("rcXY84C4g14iFp6taFXjjQGVeHqSCh9RX", createWalletInfos("GateHub")); // DASH
        known.put("rcRzGWq6Ng3jeYhqnmM4zcWcUh69hrQ8V", createWalletInfos("GateHub")); // LTC
        known.put("rcvxE9PS9YBwxtGg1qNeewV6ZB3wGubZq", createWalletInfos("GateHub")); // USDT
        known.put("rcEGREd8NmkKRE8GE424sksyt1tJVFZwu", createWalletInfos("GateHub")); // USDC
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
        return res.getString("staticinformation");
    }

    @Override
    public InfoType[] supportedTypes() {
        return new InfoType[]{InfoType.Name};
    }
}
