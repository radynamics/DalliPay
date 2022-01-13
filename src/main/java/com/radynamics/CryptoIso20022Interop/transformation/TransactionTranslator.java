package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.iso20022.OtherAccount;

public class TransactionTranslator {
    private TransformInstruction transformInstruction;

    public TransactionTranslator(TransformInstruction transformInstruction) {
        this.transformInstruction = transformInstruction;
    }

    public Transaction[] apply(Transaction[] transactions) {
        for (var t : transactions) {
            {
                var account = transformInstruction.getAccountOrNull(t.getSenderWallet());
                account = account == null ? new OtherAccount(t.getSenderWallet().getPublicKey()) : account;
                t.setSenderAccount(account);
            }
            {
                var account = transformInstruction.getAccountOrNull(t.getReceiverWallet());
                account = account == null ? new OtherAccount(t.getReceiverWallet().getPublicKey()) : account;
                t.setReceiverAccount(account);
            }
        }

        return transactions;
    }
}
