package com.radynamics.CryptoIso20022Interop.exchange;

public class CurrencyPair {
    private String first;
    private String second;

    public CurrencyPair(String first, String second) {
        if (first == null) throw new IllegalArgumentException("Parameter 'first' cannot be null");
        if (first.length() == 0) throw new IllegalArgumentException("Parameter 'first' cannot be empty");
        if (second == null) throw new IllegalArgumentException("Parameter 'second' cannot be null");
        if (second.length() == 0) throw new IllegalArgumentException("Parameter 'second' cannot be empty");
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

    public String getDisplayText() {
        return String.format("%s/%s", getFirst(), getSecond());
    }

    public boolean affects(String ccy) {
        return getFirst().equals(ccy) || getSecond().equals(ccy);
    }

    public CurrencyPair invert() {
        return new CurrencyPair(getSecond(), getFirst());
    }

    public boolean sameAs(CurrencyPair other) {
        if (other == null) return false;
        return getFirst().equals(other.getFirst()) && getSecond().equals(other.getSecond());
    }

    public boolean isOneToOne() {
        return getFirst().equals(getSecond());
    }

    @Override
    public String toString() {
        return String.format("first=%s, second=%s", first, second);
    }
}
