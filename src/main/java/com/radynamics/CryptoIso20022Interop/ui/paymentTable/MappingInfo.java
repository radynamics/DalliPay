package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.db.AccountMapping;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public class MappingInfo {
    private final AccountMapping mapping;
    private final ChangedValue changedValue;

    public MappingInfo(AccountMapping mapping, ChangedValue changedValue) {
        this.mapping = mapping;
        this.changedValue = changedValue;
    }

    public boolean apply(Payment p) {
        switch (changedValue) {
            case SenderAccount -> {
                if (affected(p.getSenderWallet())) {
                    p.setSenderAccount(mapping.getAccount());
                    return true;
                }
                return false;
            }
            case SenderWallet -> {
                if (affected(p.getSenderAccount())) {
                    p.setSenderWallet(mapping.getWallet());
                    return true;
                }
                return false;
            }
            case ReceiverAccount -> {
                if (affected(p.getReceiverWallet())) {
                    p.setReceiverAccount(mapping.getAccount());
                    return true;
                }
                return false;
            }
            case ReceiverWallet -> {
                if (affected(p.getReceiverAccount())) {
                    p.setReceiverWallet(mapping.getWallet());
                    return true;
                }
                return false;
            }
            default -> throw new IllegalStateException("Unexpected value: " + changedValue);
        }
    }

    private boolean affected(Account account) {
        if (mapping.getAccount() == null || account == null) {
            return false;
        }

        return account.getUnformatted().equals(mapping.getAccount().getUnformatted());
    }

    private boolean affected(Wallet wallet) {
        if (mapping.getWallet() == null || wallet == null) {
            return false;
        }

        return wallet.getPublicKey().equals(mapping.getWallet().getPublicKey());
    }
}
