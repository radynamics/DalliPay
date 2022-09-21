package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Bithomp;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XrpScan;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XrplOrg;
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
                } else if (lookupProviderId.equals(XrplOrg.Id)) {
                    return new XrplOrg(networkType);
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
                } else if (lookupProviderId.equals(XrplOrg.Id)) {
                    return new XrplOrg(networkType);
                } else if (lookupProviderId.equals(XrpScan.Id)) {
                    return createXrpScan(networkType);
                }
                throw new IllegalStateException("Unexpected value: " + lookupProviderId);
            }
            default -> throw new IllegalStateException("Unexpected value: " + ledger.getId());
        }
    }

    public static String[] allIds(LedgerId id) {
        switch (id) {
            case Xrpl -> {
                return new String[]{Bithomp.Id, XrplOrg.Id, XrpScan.Id};
            }
            default -> throw new IllegalStateException("Unexpected value: " + id);
        }
    }

    public static String getDisplayText(String lookupProviderId) {
        if (lookupProviderId.equals(Bithomp.Id)) {
            return Bithomp.displayName;
        } else if (lookupProviderId.equals(XrplOrg.Id)) {
            return XrplOrg.displayName;
        } else if (lookupProviderId.equals(XrpScan.Id)) {
            return XrpScan.displayName;
        }
        throw new IllegalStateException("Unexpected value: " + lookupProviderId);
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
            throw new LookupProviderException(String.format("%s doesn't support test network.", XrpScan.displayName));
        }
        return new XrpScan();
    }
}
