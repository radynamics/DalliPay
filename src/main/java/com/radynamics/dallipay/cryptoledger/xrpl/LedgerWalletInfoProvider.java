package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.WalletInfo;
import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;
import com.radynamics.dallipay.cryptoledger.generic.walletinfo.InfoType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class LedgerWalletInfoProvider implements WalletInfoProvider {
    private final Ledger ledger;
    private final DomainVerifier domainVerifier;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public LedgerWalletInfoProvider(Ledger ledger) {
        this.ledger = ledger;
        this.domainVerifier = new DomainVerifier(ledger);
    }

    public WalletInfo[] list(com.radynamics.dallipay.cryptoledger.Wallet wallet) {
        if (!ledger.isValidPublicKey(wallet.getPublicKey())) {
            return new WalletInfo[0];
        }

        var list = new ArrayList<WalletInfo>();

        var domain = ledger.getAccountDomain(WalletConverter.from(wallet));
        if (!StringUtils.isAllEmpty(domain)) {
            var wi = new WalletInfo(this, domain, InfoType.Domain);
            wi.setVerified(domainVerifier.isValid(wallet, domain));
            list.add(wi);
        }

        return list.toArray(new WalletInfo[0]);
    }

    @Override
    public String getDisplayText() {
        return String.format(res.getString("ledgerValues"), ledger.getId());
    }

    @Override
    public InfoType[] supportedTypes() {
        return new InfoType[]{InfoType.Domain};
    }
}
