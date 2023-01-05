package com.radynamics.CryptoIso20022Interop.cryptoledger.signing;

import java.awt.*;

public interface TransactionSubmitterFactory {
    TransactionSubmitter create(String id, Component parentComponent);

    TransactionSubmitter[] all(Component parentComponent);
}
