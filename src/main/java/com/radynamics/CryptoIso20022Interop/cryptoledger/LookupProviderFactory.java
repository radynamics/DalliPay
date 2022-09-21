package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Bithomp;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XrpScan;
import com.radynamics.CryptoIso20022Interop.db.ConfigRepo;

public class LookupProviderFactory {
    public static WalletLookupProvider createWalletLookupProvider(Ledger ledger) throws LookupProviderException {
        String lookupProviderId = loadProviderIdOrNull();
        var networkType = ledger.getNetwork().getType();
        switch (ledger.getId()) {
            case Xrpl -> {
                lookupProviderId = lookupProviderId == null ? Bithomp.Id : lookupProviderId;
                if (lookupProviderId.equals(Bithomp.Id)) {
                    return new Bithomp(networkType);
                } else if (lookupProviderId.equals(XrpScan.Id)) {
                    return createXrpScan(networkType);
                }
                throw new IllegalStateException("Unexpected value: " + lookupProviderId);
            }
            default -> throw new IllegalStateException("Unexpected value: " + ledger.getId());
        }
    }

    public static TransactionLookupProvider createTransactionLookupProvider(Ledger ledger) throws LookupProviderException {
        String lookupProviderId = loadProviderIdOrNull();
        var networkType = ledger.getNetwork().getType();
        switch (ledger.getId()) {
            case Xrpl -> {
                lookupProviderId = lookupProviderId == null ? Bithomp.Id : lookupProviderId;
                if (lookupProviderId.equals(Bithomp.Id)) {
                    return new Bithomp(networkType);
                } else if (lookupProviderId.equals(XrpScan.Id)) {
                    return createXrpScan(networkType);
                }
                throw new IllegalStateException("Unexpected value: " + lookupProviderId);
            }
            default -> throw new IllegalStateException("Unexpected value: " + ledger.getId());
        }
    }

    private static String loadProviderIdOrNull() throws LookupProviderException {
        try (var repo = new ConfigRepo()) {
            return repo.getLookupProviderId();
        } catch (Exception e) {
            throw new LookupProviderException("Error loading lookupProvider from config.", e);
        }
    }

    private static XrpScan createXrpScan(Network network) throws LookupProviderException {
        if (network == Network.Test) {
            throw new LookupProviderException(String.format("XrpScan.com doesn't support test network."));
        }
        return new XrpScan();
    }
}
