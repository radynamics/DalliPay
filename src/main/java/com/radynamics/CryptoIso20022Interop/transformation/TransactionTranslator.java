package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.OtherAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.math.BigDecimal;

public class TransactionTranslator {
    private final TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;
    private String targetCcy;

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
            if (t.getSenderWallet() == null) {
                t.setSenderWallet(transformInstruction.getWalletOrNull(t.getSenderAccount()));
            }
            if (t.getReceiverWallet() == null) {
                t.setReceiverWallet(transformInstruction.getWalletOrNull(t.getReceiverAccount()));
            }

            var targetCcy = getTargetCcy(t);
            if (t.getLedgerCcy().equalsIgnoreCase(targetCcy)) {
                if (t.isAmountUnknown()) {
                    t.setAmount(BigDecimal.valueOf(t.getAmountLedgerUnit()), t.getLedgerCcy());
                    t.setExchangeRate(ExchangeRate.None(t.getLedgerCcy()));
                } else {
                    t.setExchangeRate(ExchangeRate.OneToOne(t.createCcyPair()));
                }
            } else {
                var ccyPair = t.isCcyUnknown()
                        ? new CurrencyPair(t.getLedgerCcy(), targetCcy)
                        : t.createCcyPair();
                if (currencyConverter.has(ccyPair)) {
                    t.setExchangeRate(currencyConverter.get(ccyPair));
                } else {
                    t.setFiatCcy(ccyPair.getSecond());
                }
            }
        }

        return transactions;
    }

    private String getTargetCcy(Payment t) {
        if (targetCcy != null) {
            return targetCcy;
        }

        return t.getFiatCcy();
    }

    private Account getAccountOrNull(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        var account = transformInstruction.getAccountOrNull(wallet);
        return account == null ? new OtherAccount(wallet.getPublicKey()) : account;
    }

    public void setTargetCcy(String targetCcy) {
        this.targetCcy = targetCcy;
    }
}
