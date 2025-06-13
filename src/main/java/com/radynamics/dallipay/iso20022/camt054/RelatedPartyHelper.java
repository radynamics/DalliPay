package com.radynamics.dallipay.iso20022.camt054;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletInfoAggregator;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class RelatedPartyHelper {
    private final WalletInfoAggregator aggregator;

    public RelatedPartyHelper(Ledger ledger) {
        aggregator = new WalletInfoAggregator(ledger.getInfoProvider());
    }

    public Optional<String> getNameOrDomain(Wallet wallet) {
        var wi = aggregator.getNameOrDomain(wallet);
        return wi == null || StringUtils.isEmpty(wi.getValue()) ? Optional.empty() : Optional.of(wi.getValue());
    }
}
