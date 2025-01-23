package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.CurrencyPair;
import com.radynamics.dallipay.exchange.ExchangeRate;
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
        var accountMappingSource = transformInstruction.getAccountMappingSource();
        try {
            accountMappingSource.open();
            return apply(transactions, new AccountMappingSourceHelper(accountMappingSource));
        } catch (AccountMappingSourceException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                accountMappingSource.close();
            } catch (AccountMappingSourceException e) {
                log.error(e.getMessage(), e);
            }
        }
        return transactions;
    }

    public synchronized Payment[] apply(Payment[] transactions, AccountMappingSourceHelper mappingSourceHelper) throws AccountMappingSourceException {
        for (var t : transactions) {
            if (t.getSenderAccount() == null) {
                t.setSenderAccount(mappingSourceHelper.getAccountOrNull(t.getSenderWallet(), t.getSenderAddress()));
            }
            if (t.getReceiverAccount() == null) {
                t.setReceiverAccount(mappingSourceHelper.getAccountOrNull(t.getReceiverWallet(), t.getReceiverAddress()));
            }
            if (t.getSenderWallet() == null) {
                t.setSenderWallet(mappingSourceHelper.getWalletOrNull(t.getSenderAccount(), t.getSenderAddress()));
            }
            if (t.getReceiverWallet() == null) {
                t.setReceiverWallet(mappingSourceHelper.getWalletOrNull(t.getReceiverAccount(), t.getReceiverAddress()));
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

    public void setDefaultSenderWallet(LedgerId ledgerId, Wallet wallet) {
        defaultSenderWallets.removeIf(w -> w.getLedgerId().textId().equals(ledgerId.textId()));
        if (wallet != null) {
            defaultSenderWallets.add(wallet);
        }
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
