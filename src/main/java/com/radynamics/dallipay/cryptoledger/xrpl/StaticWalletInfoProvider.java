package com.radynamics.dallipay.cryptoledger.xrpl;

public class StaticWalletInfoProvider extends com.radynamics.dallipay.cryptoledger.generic.walletinfo.StaticWalletInfoProvider {

    public StaticWalletInfoProvider(Ledger ledger) {
        super(ledger);

        add("rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq", "GateHub"); // USD, EUR
        add("rhotcWYdfn6qxhVMbPKGDF3XCKqwXar5J4", "GateHub"); // USD, EUR
        add("r4GN9eEoz9K4BhMQXe4H1eYNtvtkwGdt8g", "GateHub"); // GBP
        add("rctArjqVvTHihekzDeecKo6mkTYTUSBNc", "GateHub"); // SGB
        add("rchGBxcD1A1C2tdxF6papQYZ8kjRKMYcL", "GateHub"); // BTC
        add("rcyS4CeCZVYvTiKcxj6Sx32ibKwcDHLds", "GateHub"); // BCH
        add("rcA8X3TVMST1n3CJeAdGk1RdRCHii7N2h", "GateHub"); // ETH
        add("rDAN8tzydyNfnNf2bfUQY6iR96UbpvNsze", "GateHub"); // ETC
        add("rcXY84C4g14iFp6taFXjjQGVeHqSCh9RX", "GateHub"); // DASH
        add("rcRzGWq6Ng3jeYhqnmM4zcWcUh69hrQ8V", "GateHub"); // LTC
        add("rcvxE9PS9YBwxtGg1qNeewV6ZB3wGubZq", "GateHub"); // USDT
        add("rcEGREd8NmkKRE8GE424sksyt1tJVFZwu", "GateHub"); // USDC
        add("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B", "Bitstamp");
        add("rsoLo2S1kiGeCcn6hCUXVrCpGMWLrRrLZz", "Sologenic");
        add("rs5hzzF7RzdV2Ub7Fv14Z1ShjuCrnL7FEX", "STASIS");
        add("rEaC7oNDB34K6CiSbSrA6xEPeVhXNgDZRY", "Xago");
        add("rEn9eRkX25wfGPLysUMAvZ84jAzFNpT5fL", "Stably");
        add("rUN5Zxt3K1AnMRJgEWywDJT8QDMMeLH5ok", "Novatti"); // AUDD
        add("rMkEuRii9w9uBMQDnWV5AA43gvYZR9JxVK", "Schuman Financial"); // EUROP
        add("rMxCKbEDwqr76QuheSUMdEGf4B9xJ8m5De", "Ripple"); // RLUSD
        add("rB3y9EPnq1ZrZP3aXgfyfdXQThzdXMrLMc", "Braza Group"); // USDB
        add("rGm7WCVp9gb4jZHWTEtGUr4dd74z2XuWhE", "Circle"); // USDC
    }
}
