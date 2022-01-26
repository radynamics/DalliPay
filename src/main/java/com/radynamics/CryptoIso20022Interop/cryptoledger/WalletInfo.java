package com.radynamics.CryptoIso20022Interop.cryptoledger;

public class WalletInfo {
    private String text;
    private String value;
    private int importance;

    public WalletInfo(String text, String value, int importance) {
        this.text = text;
        this.value = value;
        this.importance = importance;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public Integer getImportance() {
        return importance;
    }
}
