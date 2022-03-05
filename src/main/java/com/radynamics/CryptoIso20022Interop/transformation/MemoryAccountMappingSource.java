package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletCompare;
import com.radynamics.CryptoIso20022Interop.db.AccountMapping;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;

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
}
