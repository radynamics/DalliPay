package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.PaymentUtils;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletCompare;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletValidator;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Validator;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class PaymentValidator implements com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator {
    private final WalletHistoryValidator historyValidator;
    private final ArrayList<Pair<Ledger, com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator>> ledgerSpecificValidators = new ArrayList<>();

    public PaymentValidator(WalletHistoryValidator historyValidator) {
        this.historyValidator = historyValidator;
    }

    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();
        list.addAll(Arrays.asList(new Validator().validate(t)));

        var wv = new WalletValidator(t.getLedger());
        if (t.getReceiverWallet() != null) {
            var walletValidations = wv.validate(t.getReceiverWallet(), "Receiver");
            list.addAll(Arrays.asList(walletValidations));

            if (walletValidations.length == 0) {
                list.addAll(Arrays.asList(t.getLedger().validateReceiver(t.getReceiverWallet())));
            }
        }

        if (t.getSenderWallet() == null) {
            list.add(new ValidationResult(ValidationState.Error, "Sender wallet is missing."));
        } else {
            var walletValidations = wv.validate(t.getSenderWallet(), "Sender");
            list.addAll(Arrays.asList(walletValidations));

            if (walletValidations.length == 0) {
                if (WalletCompare.isSame(t.getSenderWallet(), t.getReceiverWallet())) {
                    list.add(new ValidationResult(ValidationState.Error, "Sender wallet is same as receiver wallet."));
                }

                var ccy = t.getAmountTransaction().getCcy();
                var currentBalance = t.getSenderWallet().getBalances().get(ccy).orElse(null);
                if (currentBalance == null) {
                    list.add(new ValidationResult(ValidationState.Error, String.format("Sender wallet balance doesn't hold %s.", ccy.getCode())));
                } else if (currentBalance.lessThan(t.getAmountTransaction())) {
                    list.add(new ValidationResult(ValidationState.Error, String.format("Sender wallet balance %s is too low.", MoneyFormatter.formatFiat(currentBalance))));
                }
            }

            list.addAll(Arrays.asList(historyValidator.validate(t)));
        }

        if (t.getExchangeRate() == null && !t.isUserCcyEqualTransactionCcy()) {
            var pair = new CurrencyPair(t.getAmountTransaction().getCcy().getCode(), t.getUserCcyCodeOrEmpty());
            list.add(new ValidationResult(ValidationState.Error, String.format("No exchange rate for %s available.", pair.getDisplayText())));
        }

        if (t.getStructuredReferences().length == 0) {
            list.add(new ValidationResult(ValidationState.Info, String.format("Remittance info is missing. Receiver won't be able to match awaited payment exactly.")));
        }

        var ledgerSpecific = getOrCreateLedgerSpecific(t.getLedger());
        if (ledgerSpecific != null) {
            list.addAll(Arrays.asList(ledgerSpecific.validate(t)));
        }

        return list.toArray(new ValidationResult[0]);
    }

    private com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator getOrCreateLedgerSpecific(Ledger ledger) {
        for (var p : ledgerSpecificValidators) {
            if (p.getKey().getId().equals(ledger.getId())) {
                return p.getValue();
            }
        }

        var v = ledger.createPaymentValidator();
        ledgerSpecificValidators.add(new ImmutablePair<>(ledger, v));
        return v;
    }

    public ValidationResult[] validate(Payment[] payments) {
        var list = new ArrayList<ValidationResult>();

        list.addAll(validateReceiverWalletsUnique(payments));

        var sendingWallets = PaymentUtils.distinctSendingWallets(payments);
        for (var w : sendingWallets) {
            var affectedPayments = PaymentUtils.fromSender(w, payments);
            Ledger l = affectedPayments.get(0).getLedger();
            var sums = PaymentUtils.sumLedgerUnit(affectedPayments);
            for (var ccy : sums.currencies()) {
                // If sender is issuer of the transferred ccy, its balance doesn't matter due it can issue always more.
                if (WalletCompare.isSame(w, ccy.getIssuer())) {
                    continue;
                }
                var balance = w.getBalances().get(ccy).orElseGet(() -> Money.zero(ccy));
                var paymentsSum = sums.sum(ccy);
                if (balance.lessThan(paymentsSum)) {
                    var paymentsSumText = MoneyFormatter.formatLedger(paymentsSum);
                    var balanceText = MoneyFormatter.formatLedger(balance);
                    list.add(new ValidationResult(ValidationState.Error, String.format("Sum of payments from %s is %s and exceeds wallet balance of %s.", w.getPublicKey(), paymentsSumText, balanceText)));
                }
            }
        }

        return list.toArray(new ValidationResult[0]);
    }

    private Collection<? extends ValidationResult> validateReceiverWalletsUnique(Payment[] payments) {
        var list = new ArrayList<ValidationResult>();

        var unique = new Hashtable<String, HashSet<String>>();
        for (var p : payments) {
            var key = p.getReceiverWallet().getPublicKey();
            if (!unique.containsKey(key)) {
                unique.put(key, new HashSet<>());
            }
            unique.get(key).add(p.getReceiverAddress() == null ? "" : p.getReceiverAddress().getName());
        }

        for (var key : unique.keySet()) {
            var count = unique.get(key).size();
            if (count > 1) {
                list.add(new ValidationResult(ValidationState.Warning, String.format("Receiver wallet %s is defined for %s different sender names.", key, count)));
            }
        }

        return list;
    }

    public WalletHistoryValidator getHistoryValidator() {
        return historyValidator;
    }

    public void clearCache() {
        for (var p : ledgerSpecificValidators) {
            p.getValue().clearCache();
        }
    }
}
