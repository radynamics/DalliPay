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

    public static final String DummyPartyId = "undefined";

    public MemoryAccountMappingSource(Ledger ledger) {
        this.ledger = ledger;
    }

    public void add(AccountMapping accountMapping) {
        accountMappings.add(accountMapping);
    }

    @Override
    public Wallet getWalletOrNull(Account account, String partyId) {
        for (var mapping : accountMappings) {
            if (mapping.getAccount().getUnformatted().equals(account.getUnformatted()) && mapping.getPartyId().equals(DummyPartyId)) {
                return mapping.getWallet();
            }
        }
        return null;
    }

    @Override
    public Account getAccountOrNull(Wallet wallet, String partyId) {
        for (var mapping : accountMappings) {
            if (WalletCompare.isSame(mapping.getWallet(), wallet) && mapping.getPartyId().equals(DummyPartyId)) {
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
