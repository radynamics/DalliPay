package com.radynamics.CryptoIso20022Interop.cryptoledger;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MoneyBag {
    private Hashtable<String, Double> amounts = new Hashtable<>();

    public Double get(String ccy) {
        for (var key : amounts.keySet()) {
            if (key.equals(ccy)) {
                return amounts.get(ccy);
            }
        }
        return 0d;
    }

    public boolean isEmpty() {
        return amounts.isEmpty();
    }

    public void set(Double amt, String ccy) {
        amounts.put(ccy, amt);
    }


    public void replaceBy(MoneyBag bag) {
        amounts = bag.amounts;
    }

    public Map<String, Double> all() {
        return amounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        for (var key : all().keySet()) {
            sb.append(String.format("%s %s", amounts.get(key), key));
        }
        return sb.toString();
    }
}
