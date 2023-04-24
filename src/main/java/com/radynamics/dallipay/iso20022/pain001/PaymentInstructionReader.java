package com.radynamics.dallipay.iso20022.pain001;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.iso20022.Payment;

import javax.swing.*;
import java.io.InputStream;

public interface PaymentInstructionReader {
    Payment[] read(InputStream input) throws Exception;

    Ledger getLedger();

    JPanel createParameterPanel();

    boolean applyParameters(JPanel parameterPanel);
}
