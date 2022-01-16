package com.radynamics.CryptoIso20022Interop.exchange;

public class CurrencyPair {
    private String first;
    private String second;

    public CurrencyPair(String first, String second) {
        this.first = first;
        this.second = second;
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }

    public static boolean contains(CurrencyPair[] list, CurrencyPair pair) {
        for (var item : list) {
            if (item.getFirst().equals(pair.getFirst()) && item.getSecond().equals(pair.getSecond())) {
                return true;
            }
        }
        return false;
    }
}
