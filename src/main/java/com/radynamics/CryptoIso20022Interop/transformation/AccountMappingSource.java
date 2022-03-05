package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.db.AccountMapping;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;

public interface AccountMappingSource {
    Wallet getWalletOrNull(Account account);

    Account getAccountOrNull(Wallet wallet);

    void add(AccountMapping mapping);
}
