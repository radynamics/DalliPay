package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Money;

import java.util.HashMap;
import java.util.Optional;

public final class FeeHelper {
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
        map.put(FeeType.LedgerTransactionFee, "Network transaction fee (editable)");
        map.put(FeeType.CurrencyTransferFee, "Transfer fee by used currency issuer");
        return map.getOrDefault(type, "unknown");
    }
}
