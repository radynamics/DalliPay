package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.google.common.primitives.UnsignedLong;
import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.NotImplementedException;

import java.math.BigDecimal;

public class TestLedger implements Ledger {
    private final static int FACTOR = 1000;

    @Override
    public LedgerId getId() {
        return null;
    }

    @Override
    public String getNativeCcySymbol() {
        return "TEST";
    }

    @Override
    public Transaction createTransaction() {
        return new TestTransaction(this, 0, getNativeCcySymbol());
    }

    @Override
    public void send(Transaction[] transactions) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public BigDecimal convertToNativeCcyAmount(long amountSmallestUnit) {
        return BigDecimal.valueOf(amountSmallestUnit / FACTOR);
    }

    @Override
    public long convertToSmallestAmount(double amountNativeCcy) {
        return (long) (amountNativeCcy * FACTOR);
    }

    @Override
    public FeeSuggestion getFeeSuggestion() {
        return new FeeSuggestion(5, 10, 15);
    }

    @Override
    public Wallet createWallet(String publicKey, String secret) {
        return new Wallet() {
            @Override
            public String getPublicKey() {
                return publicKey;
            }

            @Override
            public String getSecret() {
                return secret;
            }

            @Override
            public void setSecret(String secret) {
                // do nothing
            }

            @Override
            public UnsignedLong getLedgerBalanceSmallestUnit() {
                return UnsignedLong.ZERO;
            }

            @Override
            public void setLedgerBalance(UnsignedLong amountSmallestUnit) {
                // do nothing
            }
        };
    }

    @Override
    public void refreshBalance(Wallet wallet) {
        // do nothing;
    }

    @Override
    public Transaction[] listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception {
        return new Transaction[0];
    }

    @Override
    public boolean exists(Wallet wallet) {
        return true;
    }

    @Override
    public boolean requiresDestinationTag(Wallet wallet) {
        return false;
    }

    @Override
    public NetworkInfo getNetwork() {
        throw new NotImplementedException();
    }

    @Override
    public void setNetwork(NetworkInfo network) {
        throw new NotImplementedException();
    }

    @Override
    public WalletLookupProvider getLookupProvider() {
        return null;
    }

    @Override
    public TransactionLookupProvider getTransactionLookupProvider() {
        return null;
    }

    @Override
    public WalletInfoProvider[] getInfoProvider() {
        return new WalletInfoProvider[0];
    }

    @Override
    public boolean isValidPublicKey(String publicKey) {
        return true;
    }

    @Override
    public HttpUrl getFallbackUrl(Network type) {
        return null;
    }
}
