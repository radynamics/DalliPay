package com.radynamics.dallipay.cryptoledger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class OnchainVerifier {
    private final static Logger log = LogManager.getLogger(OnchainVerifier.class);

    private final Ledger ledger;

    public OnchainVerifier(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
    }

    public boolean verify(String transactionId, Transaction expected) {
        if (expected == null) throw new IllegalArgumentException("Parameter 'expected' cannot be null");
        var actual = getOrNull(transactionId);
        if (actual == null) {
            log.warn(String.format("Could not find on chain transactionId %s to verify result.", transactionId));
            return false;
        }
        return areEqual(expected, actual);
    }

    private Transaction getOrNull(String transactionId) {
        var maxWait = Duration.ofSeconds(20);
        var waited = Duration.ZERO;
        while (waited.toMillis() <= maxWait.toMillis()) {
            var t = ledger.getTransaction(transactionId);
            if (t != null) {
                return t;
            }

            var wait = Duration.ofMillis(500);
            try {
                Thread.sleep(wait.toMillis());
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            waited = waited.plus(wait);
        }

        return null;
    }

    private boolean areEqual(Transaction expected, Transaction actual) {
        if (!WalletCompare.isSame(expected.getReceiverWallet(), actual.getReceiverWallet())) {
            log.warn(String.format("Receiver is not equal for %s. Expected %s, actual %s", actual.getId(), expected.getReceiverWallet().getPublicKey(), actual.getReceiverWallet().getPublicKey()));
            return false;
        }

        if (!expected.getAmount().equals(actual.getAmount())) {
            log.warn(String.format("Amount is not equal for %s. Expected %s, actual %s", actual.getId(), expected.getAmount(), actual.getAmount()));
            return false;
        }

        return true;
    }
}
