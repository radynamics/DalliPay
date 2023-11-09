package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.cryptoledger.xrpl.Bithomp;
import com.radynamics.dallipay.cryptoledger.xrpl.XrpScan;
import com.radynamics.dallipay.cryptoledger.xrpl.XrplOrg;
import com.radynamics.dallipay.db.ConfigRepo;

import java.util.Optional;

public class LookupProviderFactory {
    public static WalletLookupProvider createWalletLookupProvider(Ledger ledger) throws LookupProviderException {
        var network = ledger.getNetwork();
        switch (ledger.getId()) {
            case Xrpl -> {
                var lookupProviderId = loadProviderIdOrNull(ledger.getId()).orElse(Bithomp.Id);
                if (lookupProviderId.equals(Bithomp.Id)) {
                    return new Bithomp(network);
                } else if (lookupProviderId.equals(XrplOrg.Id)) {
                    return new XrplOrg(network);
                } else if (lookupProviderId.equals(XrpScan.Id)) {
                    return new XrpScan(network);
                }
                throw new IllegalStateException("Unexpected value: " + lookupProviderId);
            }
            default -> throw new IllegalStateException("Unexpected value: " + ledger.getId());
        }
    }

    public static TransactionLookupProvider createTransactionLookupProvider(Ledger ledger) throws LookupProviderException {
        var network = ledger.getNetwork();
        switch (ledger.getId()) {
            case Xrpl -> {
                var lookupProviderId = loadProviderIdOrNull(ledger.getId()).orElse(Bithomp.Id);
                if (lookupProviderId.equals(Bithomp.Id)) {
                    return new Bithomp(network);
                } else if (lookupProviderId.equals(XrplOrg.Id)) {
                    return new XrplOrg(network);
                } else if (lookupProviderId.equals(XrpScan.Id)) {
                    return new XrpScan(network);
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

    private static Optional<String> loadProviderIdOrNull(LedgerId ledgerId) throws LookupProviderException {
        try (var repo = new ConfigRepo()) {
            return repo.getLookupProviderId(ledgerId);
        } catch (Exception e) {
            throw new LookupProviderException("Error loading lookupProvider from config.", e);
        }
    }
}
