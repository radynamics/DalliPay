package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet;

public interface TransactionSource {
    Transaction[] listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception;
}
