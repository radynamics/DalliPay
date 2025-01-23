package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.iso20022.Account;
import com.radynamics.dallipay.iso20022.Address;
import com.radynamics.dallipay.iso20022.OtherAccount;

public class AccountMappingSourceHelper {
    private final AccountMappingSource accountMappingSource;

    public AccountMappingSourceHelper(AccountMappingSource accountMappingSource) {
        this.accountMappingSource = accountMappingSource;
    }

    public Account getAccountOrNull(Wallet wallet, Address address) throws AccountMappingSourceException {
        if (wallet == null) {
            return null;
        }
        var account = accountMappingSource.getAccountOrNull(wallet, Address.createPartyIdOrEmpty(address));
        return account == null ? new OtherAccount(wallet.getPublicKey()) : account;
    }

    public Wallet getWalletOrNull(Account account, Address address) throws AccountMappingSourceException {
        if (account == null) {
            return null;
        }
        return accountMappingSource.getWalletOrNull(account, Address.createPartyIdOrEmpty(address));
    }
}
