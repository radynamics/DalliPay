package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.NetworkInfo;

import java.util.Optional;

public class XrplUtils {
    public static Optional<Integer> networkId(NetworkInfo networkInfo) {
        // For compatibility with existing chains, the NetworkID field must be omitted on any network with a Network ID of 1024 or less,
        // but must be included on any network with a Network ID of 1025 or greater. (https://xrpl.org/transaction-common-fields.html#networkid-field)
        if (networkInfo != null && networkInfo.getNetworkId() != null && networkInfo.getNetworkId() >= 1025) {
            return Optional.of(networkInfo.getNetworkId());
        }
        return Optional.empty();
    }
}
