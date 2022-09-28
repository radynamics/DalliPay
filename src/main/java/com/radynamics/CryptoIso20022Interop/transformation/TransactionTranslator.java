package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.CurrencyRefresher;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.OtherAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public class TransactionTranslator {
    private final TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;
    private Currency targetCcy;

    public TransactionTranslator(TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;
    }

    private Payment[] apply(Payment transaction) {
        return apply(new Payment[]{transaction});
    }

    public Payment[] apply(Payment[] transactions) {
        for (var t : transactions) {
            if (t.getSenderAccount() == null) {
                t.setSenderAccount(getAccountOrNull(t.getSenderWallet()));
            }
            if (t.getReceiverAccount() == null) {
                t.setReceiverAccount(getAccountOrNull(t.getReceiverWallet()));
            }
            if (t.getSenderWallet() == null) {
                t.setSenderWallet(transformInstruction.getWalletOrNull(t.getSenderAccount()));
            }
            if (t.getReceiverWallet() == null) {
                t.setReceiverWallet(transformInstruction.getWalletOrNull(t.getReceiverAccount()));
            }

            var targetCcy = getTargetCcy(t);
            if (t.getAmountTransaction().getCcy().sameCode(targetCcy)) {
                if (t.isAmountUnknown()) {
                    t.setAmount(t.getAmountTransaction());
                    t.setExchangeRate(ExchangeRate.None(t.getAmountTransaction().getCcy().getCode()));
                } else {
                    t.setExchangeRate(ExchangeRate.OneToOne(t.createCcyPair()));
                }
            } else {
                var ccyPair = t.isCcyUnknown()
                        ? new CurrencyPair(t.getAmountTransaction().getCcy(), targetCcy)
                        : t.createCcyPair();
                if (currencyConverter.has(ccyPair)) {
                    t.setExchangeRate(currencyConverter.get(ccyPair));
                } else {
                    t.setUserCcy(ccyPair.getSecond());
                }
            }
        }

        return transactions;
    }

    private Currency getTargetCcy(Payment t) {
        return targetCcy == null ? t.getUserCcy() : targetCcy;

    }

    private Account getAccountOrNull(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        var account = transformInstruction.getAccountOrNull(wallet);
        return account == null ? new OtherAccount(wallet.getPublicKey()) : account;
    }

    public void applyUserCcy(Payment payment) {
        applyUserCcy(new Payment[]{payment});
    }

    public void applyUserCcy(Payment[] payments) {
        var cr = new CurrencyRefresher();
        for (var t : payments) {
            cr.refresh(t);
            if (t.getUserCcy().getIssuer() == null) {
                apply(t);
            }
        }
    }

    public void setTargetCcy(Currency targetCcy) {
        this.targetCcy = targetCcy;
    }
}
