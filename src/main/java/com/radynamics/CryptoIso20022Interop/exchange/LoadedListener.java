package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public interface LoadedListener {
    void onLoaded(Payment t);
}
