package com.radynamics.CryptoIso20022Interop.exchange;

public class CurrencyPair {
    private Currency first;
    private Currency second;

    public CurrencyPair(String first, String second) {
        this(new Currency(first), new Currency(second));
    }

    public CurrencyPair(Currency first, Currency second) {
        if (first == null) throw new IllegalArgumentException("Parameter 'first' cannot be null");
        if (second == null) throw new IllegalArgumentException("Parameter 'second' cannot be null");
        this.first = first;
        this.second = second;
    }

    public String getFirstCode() {
        return first.getCode();
    }

    public Currency getFirst() {
        return first;
    }

    public String getSecondCode() {
        return second.getCode();
    }

    public Currency getSecond() {
        return second;
    }

    public static boolean contains(CurrencyPair[] list, CurrencyPair pair) {
        for (var item : list) {
            if (item.getFirstCode().equals(pair.getFirstCode()) && item.getSecondCode().equals(pair.getSecondCode())) {
                return true;
            }
        }
        return false;
    }

    public String getDisplayText() {
        return String.format("%s/%s", getFirstCode(), getSecondCode());
    }

    public boolean affects(String ccy) {
        return getFirstCode().equals(ccy) || getSecondCode().equals(ccy);
    }

    public CurrencyPair invert() {
        return new CurrencyPair(getSecondCode(), getFirstCode());
    }

    public boolean sameAs(CurrencyPair other) {
        if (other == null) return false;
        return getFirstCode().equals(other.getFirstCode()) && getSecondCode().equals(other.getSecondCode());
    }

    public boolean isOneToOne() {
        return getFirstCode().equals(getSecondCode());
    }

    @Override
    public String toString() {
        return String.format("first=%s, second=%s", first, second);
    }
}
