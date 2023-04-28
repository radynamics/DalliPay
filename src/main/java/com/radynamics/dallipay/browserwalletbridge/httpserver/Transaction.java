package com.radynamics.dallipay.browserwalletbridge.httpserver;

public interface Transaction {
    double getAmount();

    String getCcy();
}
