package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

public interface MappingChangedListener {
    void onWalletChanged(MappingInfo mi);

    void onAccountChanged(MappingInfo mi);
}
