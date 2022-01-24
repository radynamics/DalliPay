package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
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
            if (t.getSenderAccount() == null) {
                t.setSenderAccount(getAccountOrNull(t.getSenderWallet()));
            }
            if (t.getReceiverAccount() == null) {
                t.setReceiverAccount(getAccountOrNull(t.getReceiverWallet()));
            }

            if (t.getLedgerCcy().equalsIgnoreCase(transformInstruction.getTargetCcy())) {
                t.setExchangeRate(ExchangeRate.None(t.getLedgerCcy()));
            } else {
                var ccyPair = new CurrencyPair(t.getLedgerCcy(), transformInstruction.getTargetCcy());
                if (currencyConverter.has(ccyPair)) {
                    var amt = transformInstruction.getLedger().convertToNativeCcyAmount(t.getLedgerAmountSmallestUnit());
                    t.setExchangeRate(currencyConverter.get(ccyPair));
                }
            }
        }

        return transactions;
    }

    private Account getAccountOrNull(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        var account = transformInstruction.getAccountOrNull(wallet);
        return account == null ? new OtherAccount(wallet.getPublicKey()) : account;
    }
}
