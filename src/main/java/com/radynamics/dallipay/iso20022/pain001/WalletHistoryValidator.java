package com.radynamics.dallipay.iso20022.pain001;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.iso20022.Payment;

public interface WalletHistoryValidator {
    ValidationResult[] validate(Payment p);

    void loadHistory(Ledger ledger, Wallet wallet);

    void clearCache();

    void setNetwork(NetworkInfo networkInfo);
}
