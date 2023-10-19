package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.NetworkInfo;

public interface SidechainChangedListener {
    void onCreated(NetworkInfo networkInfo);
}
