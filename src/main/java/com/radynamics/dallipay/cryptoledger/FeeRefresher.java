package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;

import java.util.HashMap;
import java.util.Map;

public class FeeRefresher {
    private final Payment[] payments;

    public FeeRefresher(Payment[] payments) {
        this.payments = payments;
    }

    public void refresh() {
        var latestFees = getFeeSuggestions();
        for (var p : payments) {
            p.setLedgerTransactionFee(latestFees.get(createKey(p)).getLow());
        }
    }

    public void refresh(Money fee) {
        for (var p : payments) {
            p.setLedgerTransactionFee(fee);
        }
    }

    private Map<String, FeeSuggestion> getFeeSuggestions() {
        var map = new HashMap<String, FeeSuggestion>();
        for (var p : payments) {
            var key = createKey(p);
            if (map.containsKey(key)) {
                continue;
            }
            map.put(key, p.getLedger().getFeeSuggestion());
        }
        return map;
    }

    private static final String createKey(Payment p) {
        return p.getLedger().getId().textId();
    }
}
