package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.OtherAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public class TransactionTranslator {
    private final TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;

    public TransactionTranslator(TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;
    }

    public Payment[] apply(Payment[] transactions) {
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

            double value;
            var amtCcy = "";
            if (t.getLedgerCcy().equalsIgnoreCase(transformInstruction.getTargetCcy())) {
                value = transformInstruction.getLedger().convertToNativeCcyAmount(t.getLedgerAmountSmallestUnit()).doubleValue();
                amtCcy = t.getLedgerCcy();
            } else {
                var amt = transformInstruction.getLedger().convertToNativeCcyAmount(t.getLedgerAmountSmallestUnit());
                // TODO: improve rounding (ex. JPY)
                value = currencyConverter.convert(amt, t.getLedgerCcy(), transformInstruction.getTargetCcy());
                amtCcy = transformInstruction.getTargetCcy();
            }
            t.setAmount(value, amtCcy);
        }

        return transactions;
    }
}
