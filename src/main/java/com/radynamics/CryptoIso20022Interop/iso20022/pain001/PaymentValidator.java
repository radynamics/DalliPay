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
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;

public class PaymentValidator implements com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator {
    private final WalletHistoryValidator historyValidator;

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

            if (walletValidations.length == 0 && WalletCompare.isSame(t.getSenderWallet(), t.getReceiverWallet())) {
                list.add(new ValidationResult(ValidationState.Error, "Sender wallet is same as receiver wallet."));
            }

            list.addAll(Arrays.asList(historyValidator.validate(t)));
        }

        if (t.getExchangeRate() == null) {
            var pair = new CurrencyPair(t.getLedgerCcy(), t.getFiatCcy());
            list.add(new ValidationResult(ValidationState.Error, String.format("No exchange rate for %s available.", pair.getDisplayText())));
        }

        if (t.getStructuredReferences().length == 0) {
            list.add(new ValidationResult(ValidationState.Info, String.format("Remittance info is missing. Receiver won't be able to match awaited payment exactly.")));
        }

        return list.toArray(new ValidationResult[0]);
    }

    public ValidationResult[] validate(Payment[] payments) {
        var list = new ArrayList<ValidationResult>();

        list.addAll(validateReceiverWalletsUnique(payments));

        var sendingWallets = PaymentUtils.distinctSendingWallets(payments);
        for (var w : sendingWallets) {
            var affectedPayments = PaymentUtils.fromSender(w, payments);
            Ledger l = affectedPayments.get(0).getLedger();
            var sums = PaymentUtils.sumLedgerUnit(affectedPayments);
            var nativeCcySum = sums.sum(l.getNativeCcySymbol());
            if (nativeCcySum > w.getLedgerBalanceSmallestUnit().longValue()) {
                var sumNativeText = MoneyFormatter.formatLedger(BigDecimal.valueOf(nativeCcySum), l.getNativeCcySymbol());
                var balanceNativeText = MoneyFormatter.formatLedger(l.convertToNativeCcyAmount(w.getLedgerBalanceSmallestUnit().longValue()), l.getNativeCcySymbol());
                list.add(new ValidationResult(ValidationState.Error, String.format("Sum of %s payments from %s is %s and exeeds wallet balance of %s.", affectedPayments.size(), w.getPublicKey(), sumNativeText, balanceNativeText)));
            }

            if (StringUtils.isAllEmpty(w.getSecret())) {
                list.add(new ValidationResult(ValidationState.Error, "Sender wallet secret (private Key) is missing."));
            } else {
                var vs = new WalletValidator(l).validateSecret(w);
                if (vs != null) {
                    list.add(vs);
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
}
