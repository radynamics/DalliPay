package com.radynamics.dallipay.cryptoledger.xrpl.api;

import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.TransactionResult;
import com.radynamics.dallipay.cryptoledger.generic.Wallet;

public interface TransactionSource {
    TransactionResult listPaymentsSent(Wallet from, long sinceDaysAgo, int limit) throws Exception;

    TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception;
}
