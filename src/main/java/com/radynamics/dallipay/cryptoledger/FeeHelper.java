package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.exchange.Money;

import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

public final class FeeHelper {
    private static final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public static Optional<Money> get(Fee[] fees, FeeType type) {
        for (var f : fees) {
            if (f.getType() == type) {
                return Optional.of(f.getAmount());
            }
        }
        return Optional.empty();
    }

    public static String getText(FeeType type) {
        var map = new HashMap<FeeType, String>();
        map.put(FeeType.LedgerTransactionFee, res.getString("feeHelper.ledgerFee"));
        map.put(FeeType.CurrencyTransferFee, res.getString("feeHelper.ccyTransferFee"));
        return map.getOrDefault(type, res.getString("unknown"));
    }
}
