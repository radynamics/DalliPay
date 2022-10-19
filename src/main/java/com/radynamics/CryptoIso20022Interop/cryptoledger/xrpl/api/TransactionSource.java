package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.TransactionResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet;

public interface TransactionSource {
    TransactionResult listPaymentsSent(Wallet from, long sinceDaysAgo, int limit) throws Exception;

    TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception;
}
