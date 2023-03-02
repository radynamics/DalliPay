package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.db.AccountMapping;
import com.radynamics.dallipay.iso20022.Account;
import com.radynamics.dallipay.iso20022.Address;
import com.radynamics.dallipay.iso20022.Payment;

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
                if (affected(p.getSenderWallet(), p.getSenderAddress())) {
                    p.setSenderAccount(mapping.getAccount());
                    return true;
                }
                return false;
            }
            case SenderWallet -> {
                if (affected(p.getSenderAccount(), p.getSenderAddress())) {
                    p.setSenderWallet(mapping.getWallet());
                    return true;
                }
                return false;
            }
            case ReceiverAccount -> {
                if (affected(p.getReceiverWallet(), p.getReceiverAddress())) {
                    p.setReceiverAccount(mapping.getAccount());
                    return true;
                }
                return false;
            }
            case ReceiverWallet -> {
                if (affected(p.getReceiverAccount(), p.getReceiverAddress())) {
                    p.setReceiverWallet(mapping.getWallet());
                    return true;
                }
                return false;
            }
            default -> throw new IllegalStateException("Unexpected value: " + changedValue);
        }
    }

    private boolean affected(Account account, Address address) {
        if (mapping.getAccount() == null && account == null) {
            return true;
        }

        if (mapping.getAccount() == null || account == null) {
            return false;
        }

        return account.getUnformatted().equals(mapping.getAccount().getUnformatted()) && affectedPartyId(address);
    }

    private boolean affected(Wallet wallet, Address address) {
        if (mapping.getWallet() == null && wallet == null) {
            return true;
        }

        if (mapping.getWallet() == null || wallet == null) {
            return false;
        }

        return wallet.getPublicKey().equals(mapping.getWallet().getPublicKey()) && affectedPartyId(address);
    }

    private boolean affectedPartyId(Address address) {
        return Address.createPartyIdOrEmpty(address).equals(mapping.getPartyId());
    }

    public AccountMapping getMapping() {
        return mapping;
    }

    public ChangedValue getChangedValue() {
        return changedValue;
    }
}
