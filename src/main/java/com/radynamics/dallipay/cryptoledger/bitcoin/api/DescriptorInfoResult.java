package com.radynamics.dallipay.cryptoledger.bitcoin.api;

public class DescriptorInfoResult {
    private final String checksum;

    public DescriptorInfoResult(String checksum) {
        this.checksum = checksum;
    }

    public String checksum() {
        return checksum;
    }
}
