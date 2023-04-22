package com.radynamics.dallipay.iso20022.pain001;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.iso20022.Account;

public final class ReaderUtils {
    static Wallet toValidWalletOrNull(Ledger ledger, Account account) {
        if (account == null || !ledger.isValidPublicKey(account.getUnformatted())) {
            return null;
        }
        return ledger.createWallet(account.getUnformatted(), null);
    }
}
