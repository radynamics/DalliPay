package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api.JsonRpcApi;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class LedgerWalletInfoProvider implements WalletInfoProvider {
    private Ledger ledger;

    public LedgerWalletInfoProvider(Ledger ledger) {
        this.ledger = ledger;
    }

    public WalletInfo[] list(com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet wallet) {
        if (!ledger.isValidPublicKey(wallet.getPublicKey())) {
            return new WalletInfo[0];
        }
        var api = new JsonRpcApi(ledger, ledger.getNetwork());

        var list = new ArrayList<WalletInfo>();

        var domain = api.getAccountDomain(WalletConverter.from(wallet));
        if (!StringUtils.isAllEmpty(domain)) {
            var wi = new WalletInfo("Domain", domain, 100);
            var dv = new DomainVerifier(ledger.getNetwork());
            wi.setVerified(dv.isValid(wallet, domain));
            list.add(wi);
        }

        return list.toArray(new WalletInfo[0]);
    }
}
