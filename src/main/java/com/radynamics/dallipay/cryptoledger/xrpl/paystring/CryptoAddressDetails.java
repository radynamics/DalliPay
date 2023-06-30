package com.radynamics.dallipay.cryptoledger.xrpl.paystring;

public class CryptoAddressDetails {
    private String address;
    private String tag;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "address: %s, tag: %s".formatted(address, tag);
    }
}
