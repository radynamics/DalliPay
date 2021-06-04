package com.radynamics.CryptoIso20022Interop.cryptoledger;

import org.apache.commons.lang3.NotImplementedException;

public final class NetworkConverter {

    public static Network from(String id) {
        switch (id.toLowerCase()) {
            case "live":
                return Network.Live;
            case "test":
                return Network.Test;
            default:
                throw new NotImplementedException(String.format("Network %s unknown.", id));
        }
    }
}
