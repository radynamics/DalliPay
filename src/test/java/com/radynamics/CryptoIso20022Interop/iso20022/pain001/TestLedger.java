package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import org.apache.commons.lang3.NotImplementedException;

import java.math.BigDecimal;

public class TestLedger implements Ledger {
    private final static int FACTOR = 1000;

    @Override
    public String getNativeCcySymbol() {
        return "TEST";
    }

    @Override
    public Transaction createTransaction(Wallet sender, Wallet receiver, long amountSmallestUnit, String ccy) {
        var t = new TestTransaction(this, amountSmallestUnit, ccy);
        t.setSender(sender);
        t.setReceiver(receiver);
        return t;
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
        };
    }

    @Override
    public Transaction[] listPayments(Wallet wallet, DateTimeRange period) throws Exception {
        return new Transaction[0];
    }

    @Override
    public boolean exists(Wallet wallet) {
        return true;
    }

    @Override
    public void setNetwork(Network network) {
        throw new NotImplementedException();
    }

    @Override
    public WalletLookupProvider getLookupProvider() {
        return null;
    }

    @Override
    public boolean isValidPublicKey(String publicKey) {
        return true;
    }
}
