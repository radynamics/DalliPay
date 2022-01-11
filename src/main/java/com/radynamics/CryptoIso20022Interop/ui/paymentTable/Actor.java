package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.Address;

public enum Actor {
    Sender,
    Receiver,
    ;

    public String get(String sender, String receiver) {
        return (String) get(sender, (Object) receiver);
    }

    public Address get(Address sender, Address receiver) {
        return (Address) get(sender, (Object) receiver);
    }

    public Account get(Account sender, Account receiver) {
        return (Account) get(sender, (Object) receiver);
    }

    public Wallet get(Wallet sender, Wallet receiver) {
        return (Wallet) get(sender, (Object) receiver);
    }

    private Object get(Object sender, Object receiver) {
        switch (this) {
            case Sender -> {
                return sender;
            }
            case Receiver -> {
                return receiver;
            }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
