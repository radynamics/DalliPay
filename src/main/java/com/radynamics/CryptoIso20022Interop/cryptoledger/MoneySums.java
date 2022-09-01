package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Money;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MoneySums {
    private final ArrayList<Pair<String, Double>> amounts = new ArrayList<>();

    public void plus(Money amt) {
        if (amt == null) throw new IllegalArgumentException("Parameter 'amt' cannot be null");
        amounts.add(new ImmutablePair<>(amt.getCcy().getCode(), amt.getNumber().doubleValue()));
    }

    public Double sum(String ccy) {
        var sum = 0.0;
        for (var amt : amounts) {
            if (amt.getKey().equals(ccy)) {
                sum += amt.getValue();
            }
        }
        return sum;
    }

    public String[] currencies() {
        var set = new HashSet<String>();
        for (var amt : amounts) {
            set.add(amt.getKey());
        }
        return set.toArray(new String[0]);
    }

    public Map<String, Double> sum() {
        var list = new ArrayList<Map.Entry<String, Double>>();
        for (var ccy : currencies()) {
            list.add(Map.entry(ccy, sum(ccy)));
        }

        list.sort(Map.Entry.comparingByValue());

        var map = new HashMap<String, Double>();
        for (var item : list) {
            map.put(item.getKey(), item.getValue());
        }
        return map;
    }

    @Override
    public String toString() {
        return String.format("size: %s", amounts.size());
    }
}
