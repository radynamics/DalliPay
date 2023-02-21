package com.radynamics.dallipay.cryptoledger.signing;

import java.awt.*;

public interface TransactionSubmitterFactory {
    TransactionSubmitter create(String id, Component parentComponent);

    TransactionSubmitter[] all(Component parentComponent);

    TransactionSubmitter getSuggested(Component parentComponent);
}
