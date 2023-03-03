package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.CurrencyPair;
import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.iso20022.Account;
import com.radynamics.dallipay.iso20022.Address;
import com.radynamics.dallipay.iso20022.OtherAccount;
import com.radynamics.dallipay.iso20022.Payment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class TransactionTranslator {
    private final static Logger log = LogManager.getLogger(TransactionTranslator.class);
    private final TransformInstruction transformInstruction;
    private final CurrencyConverter currencyConverter;
    private Currency targetCcy;
    private ArrayList<Wallet> defaultSenderWallets = new ArrayList<>();

    public TransactionTranslator(TransformInstruction transformInstruction, CurrencyConverter currencyConverter) {
        this.transformInstruction = transformInstruction;
        this.currencyConverter = currencyConverter;
    }

    private Payment[] apply(Payment transaction) {
        return apply(new Payment[]{transaction});
    }

    public synchronized Payment[] apply(Payment[] transactions) {
        try {
            transformInstruction.getAccountMappingSource().open();
            for (var t : transactions) {
                if (t.getSenderAccount() == null) {
                    t.setSenderAccount(getAccountOrNull(t.getSenderWallet(), t.getSenderAddress()));
                }
                if (t.getReceiverAccount() == null) {
                    t.setReceiverAccount(getAccountOrNull(t.getReceiverWallet(), t.getReceiverAddress()));
                }
                if (t.getSenderWallet() == null) {
                    t.setSenderWallet(transformInstruction.getWalletOrNull(t.getSenderAccount(), t.getSenderAddress()));
                }
                if (t.getReceiverWallet() == null) {
                    t.setReceiverWallet(transformInstruction.getWalletOrNull(t.getReceiverAccount(), t.getReceiverAddress()));
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
        } catch (AccountMappingSourceException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                transformInstruction.getAccountMappingSource().close();
            } catch (AccountMappingSourceException e) {
                log.error(e.getMessage(), e);
            }
        }

        return transactions;
    }

    public void applyDefaultSender(Collection<Payment> transactions) {
        for (var t : transactions) {
            applyDefaultSender(t);
        }
    }

    public void applyDefaultSender(Payment t) {
        if (t.getSenderWallet() != null) {
            return;
        }

        t.setSenderWallet(getDefaultSenderWallet(t.getLedger()));
    }

    private Currency getTargetCcy(Payment t) {
        return targetCcy == null ? t.getUserCcy() : targetCcy;

    }

    private Account getAccountOrNull(Wallet wallet, Address address) throws AccountMappingSourceException {
        if (wallet == null) {
            return null;
        }
        var account = transformInstruction.getAccountOrNull(wallet, address);
        return account == null ? new OtherAccount(wallet.getPublicKey()) : account;
    }

    public void applyUserCcy(Payment payment) {
        applyUserCcy(new Payment[]{payment});
    }

    public void applyUserCcy(Payment[] payments) {
        for (var t : payments) {
            var paths = t.getLedger().createPaymentPathFinder().find(currencyConverter, t);
            if (paths.length == 0) {
                continue;
            }
            Arrays.sort(paths, (a, b) -> Integer.compare(b.getRank(), a.getRank()));
            paths[0].apply(t);
            if (t.getUserCcy().getIssuer() == null) {
                apply(t);
            }
        }
    }

    public void setTargetCcy(Currency targetCcy) {
        this.targetCcy = targetCcy;
    }

    public void setDefaultSenderWallet(Wallet wallet) {
        defaultSenderWallets.removeIf(w -> w.getLedgerId().textId().equals(wallet.getLedgerId().textId()));
        defaultSenderWallets.add(wallet);
    }

    private Wallet getDefaultSenderWallet(Ledger ledger) {
        for (var w : defaultSenderWallets) {
            if (w.getLedgerId().textId().equals(ledger.getId().textId())) {
                return w;
            }
        }
        return null;
    }
}
