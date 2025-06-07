package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.db.AccountMapping;
import com.radynamics.dallipay.iso20022.AccountFactory;
import com.radynamics.dallipay.paymentrequest.AccountWalletPair;

import java.util.ArrayList;
import java.util.List;

public class AccountMappingConverter {
    public static List<AccountMapping> convert(Ledger ledger, List<AccountWalletPair> accountWalletPairs) {
        var list = new ArrayList<AccountMapping>();
        for (var pair : accountWalletPairs) {
            var mapping = new AccountMapping(ledger);
            list.add(mapping);
            mapping.setAccount(AccountFactory.create(pair.accountNo()));
            mapping.setWallet(ledger.createWallet(pair.walletPublicKey(), ""));
            mapping.setPartyId(AccountMapping.NO_PARTY);
        }
        return list;
    }
}
