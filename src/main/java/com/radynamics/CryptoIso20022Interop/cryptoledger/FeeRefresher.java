package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.util.HashMap;
import java.util.Map;

public class FeeRefresher {
    private final Payment[] payments;

    public FeeRefresher(Payment[] payments) {
        this.payments = payments;
    }

    public void refresh() {
        var latestFees = getLatestFees();
        for (var p : payments) {
            p.setFeeSmallestUnit(latestFees.get(createKey(p)));
        }
    }

    private Map<String, Long> getLatestFees() {
        var map = new HashMap<String, Long>();
        for (var p : payments) {
            var key = createKey(p);
            if (map.containsKey(key)) {
                continue;
            }
            map.put(key, p.getLedger().getLatestFeeSmallestUnit());
        }
        return map;
    }

    private static final String createKey(Payment p) {
        return p.getLedger().getId();
    }
}
