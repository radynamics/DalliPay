package com.radynamics.dallipay.iso20022.pain001;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.iso20022.Payment;

public class NullHistoryValidator implements WalletHistoryValidator {
    @Override
    public ValidationResult[] validate(Payment p) {
        return new ValidationResult[0];
    }

    @Override
    public void loadHistory(Ledger ledger, Wallet wallet) {
        // do nothing
    }

    @Override
    public void clearCache() {
        // do nothing
    }

    @Override
    public void setNetwork(NetworkInfo networkInfo) {
        // do nothing
    }
}
