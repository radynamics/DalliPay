package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletCompare;
import com.radynamics.dallipay.db.AccountMapping;
import com.radynamics.dallipay.iso20022.Account;

import java.util.ArrayList;

public class MemoryAccountMappingSource implements AccountMappingSource {
    private final ArrayList<AccountMapping> accountMappings = new ArrayList<>();
    private final Ledger ledger;

    public MemoryAccountMappingSource(Ledger ledger) {
        this.ledger = ledger;
    }

    public void add(AccountMapping accountMapping) {
        accountMappings.add(accountMapping);
    }

    @Override
    public Wallet getWalletOrNull(Account account) {
        for (var mapping : accountMappings) {
            if (mapping.getAccount().getUnformatted().equals(account.getUnformatted())) {
                return mapping.getWallet();
            }
        }
        return null;
    }

    @Override
    public Account getAccountOrNull(Wallet wallet) {
        for (var mapping : accountMappings) {
            if (WalletCompare.isSame(mapping.getWallet(), wallet)) {
                return mapping.getAccount();
            }
        }
        return null;
    }

    @Override
    public void open() {
        // do nothing
    }

    @Override
    public void close() {
        // do nothing
    }
}
