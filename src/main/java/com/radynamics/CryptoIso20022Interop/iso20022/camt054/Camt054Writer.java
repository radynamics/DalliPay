package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public interface Camt054Writer {
    Object createDocument(Payment[] transactions) throws Exception;
}
