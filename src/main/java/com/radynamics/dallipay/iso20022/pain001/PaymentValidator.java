package com.radynamics.dallipay.iso20022.pain001;

import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationState;
import com.radynamics.dallipay.cryptoledger.transaction.Validator;
import com.radynamics.dallipay.exchange.CurrencyPair;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class PaymentValidator implements com.radynamics.dallipay.iso20022.PaymentValidator {
    private final WalletHistoryValidator historyValidator;
    private final ArrayList<Pair<Ledger, com.radynamics.dallipay.iso20022.PaymentValidator>> ledgerSpecificValidators = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Validations");

    public PaymentValidator(WalletHistoryValidator historyValidator) {
        this.historyValidator = historyValidator;
    }

    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();
        list.addAll(Arrays.asList(new Validator().validate(t)));

        var wv = new WalletValidator(t.getLedger());
        if (t.getReceiverWallet() != null) {
            var walletValidations = wv.validate(t.getReceiverWallet(), res.getString("receiver"));
            list.addAll(Arrays.asList(walletValidations));
        }

        if (t.getSenderWallet() == null) {
            list.add(new ValidationResult(ValidationState.Error, res.getString("senderWalletMissing")));
        } else {
            var walletValidations = wv.validate(t.getSenderWallet(), res.getString("sender"));
            list.addAll(Arrays.asList(walletValidations));

            if (walletValidations.length == 0) {
                if (WalletCompare.isSame(t.getSenderWallet(), t.getReceiverWallet())) {
                    list.add(new ValidationResult(ValidationState.Error, res.getString("senderWalletSameAsReceiver")));
                }

                var ccy = t.getAmountTransaction().getCcy();
                var currentBalance = t.getSenderWallet().getBalances().get(ccy).orElse(null);
                if (currentBalance == null) {
                    if (!t.getSubmitter().supportsPathFinding()) {
                        list.add(new ValidationResult(ValidationState.Error, String.format(res.getString("senderDoesntHold"), ccy.getCode())));
                    }
                } else if (currentBalance.lessThan(t.getAmountTransaction())) {
                    list.add(new ValidationResult(ValidationState.Error, String.format(res.getString("senderBalanceTooLow"), MoneyFormatter.formatFiat(currentBalance))));
                }
            }

            list.addAll(Arrays.asList(historyValidator.validate(t)));
        }

        if (t.getExchangeRate() == null && !t.isUserCcyEqualTransactionCcy()) {
            var pair = new CurrencyPair(t.getAmountTransaction().getCcy().getCode(), t.getUserCcyCodeOrEmpty());
            list.add(new ValidationResult(ValidationState.Error, String.format(res.getString("missingFxRate"), pair.getDisplayText())));
        }

        if (t.getStructuredReferences().length == 0) {
            list.add(new ValidationResult(ValidationState.Info, String.format(res.getString("missingRemittanceInfo"))));
        }

        var ledgerSpecific = getOrCreateLedgerSpecific(t.getLedger());
        if (ledgerSpecific != null) {
            list.addAll(Arrays.asList(ledgerSpecific.validate(t)));
        }

        if (t.getExpectedCurrency() != null) {
            var expected = t.getExpectedCurrency().getIssuer();
            var actualCcy = t.getAmountTransaction().getCcy();
            if (!WalletCompare.isSame(expected, actualCcy.getIssuer())) {
                var aggregator = new WalletInfoAggregator(t.getLedger().getInfoProvider());
                var issuerText = WalletInfoFormatter.format(expected, aggregator.getNameOrDomain(expected), true);
                var msg = String.format(res.getString("receiverExpectsAmtInCcy"), issuerText, actualCcy);
                list.add(new ValidationResult(ValidationState.Info, msg));
            }
        }

        return list.toArray(new ValidationResult[0]);
    }

    private com.radynamics.dallipay.iso20022.PaymentValidator getOrCreateLedgerSpecific(Ledger ledger) {
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
            if (affectedPayments.get(0).getSubmitter().supportsPathFinding()) {
                continue;
            }
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
                    list.add(new ValidationResult(ValidationState.Error, String.format(res.getString("paymentSumExeeds"), w.getPublicKey(), paymentsSumText, balanceText)));
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
                list.add(new ValidationResult(ValidationState.Warning, String.format(res.getString("sameReceiverWalletDifferentSender"), key, count)));
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
