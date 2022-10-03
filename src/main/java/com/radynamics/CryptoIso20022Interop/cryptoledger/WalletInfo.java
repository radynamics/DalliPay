package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.walletinfo.InfoType;

public class WalletInfo {
    private String text;
    private String value;
    private int importance;
    private InfoType type;
    private boolean verified;

    public WalletInfo(String text, boolean value, int importance) {
        this(text, String.valueOf(value), importance);
    }

    public WalletInfo(String text, String value, int importance) {
        this(text, value, importance, InfoType.Undefined);
    }

    public WalletInfo(String text, String value, int importance, InfoType type) {
        this.text = text;
        this.value = value;
        this.importance = importance;
        this.type = type;
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

    public boolean getVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public InfoType getType() {
        return type;
    }
}
