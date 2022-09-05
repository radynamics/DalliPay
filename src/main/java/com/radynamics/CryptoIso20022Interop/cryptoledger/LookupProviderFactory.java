package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Bithomp;

public class LookupProviderFactory {
    public static WalletLookupProvider createWalletLookupProvider(LedgerId ledgerId, NetworkInfo network) throws LookupProviderException {
        switch (ledgerId) {
            case Xrpl -> {
                return new Bithomp(network.getType());
            }
            default -> throw new IllegalStateException("Unexpected value: " + ledgerId);
        }
    }

    public static TransactionLookupProvider createTransactionLookupProvider(LedgerId ledgerId, NetworkInfo network) throws LookupProviderException {
        switch (ledgerId) {
            case Xrpl -> {
                return new Bithomp(network.getType());
            }
            default -> throw new IllegalStateException("Unexpected value: " + ledgerId);
        }
    }
}
