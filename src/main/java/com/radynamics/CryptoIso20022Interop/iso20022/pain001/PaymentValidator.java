package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletValidator;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Validator;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class PaymentValidator implements com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator {
    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();
        list.addAll(Arrays.asList(new Validator().validate(t)));

        var wv = new WalletValidator(t.getLedger());
        if (t.getReceiverWallet() != null) {
            list.addAll(Arrays.asList(wv.validate(t.getReceiverWallet())));
        }

        if (t.getSenderWallet() == null) {
            list.add(new ValidationResult(ValidationState.Error, "Sender wallet is missing."));
        } else {
            list.addAll(Arrays.asList(wv.validate(t.getSenderWallet())));
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

        var sendingWallets = getSendingWalletsOf(payments);
        for (var w : sendingWallets) {
            var affectedPayments = filterSendingWallet(w, payments);
            long sum = 0;
            for (var p : affectedPayments) {
                sum += p.getLedgerAmountSmallestUnit();
            }

            if (sum > w.getLedgerBalanceSmallestUnit().longValue()) {
                Ledger l = affectedPayments.get(0).getLedger();
                var sumNativeText = MoneyFormatter.formatLedger(l.convertToNativeCcyAmount(sum), l.getNativeCcySymbol());
                var balanceNativeText = MoneyFormatter.formatLedger(l.convertToNativeCcyAmount(w.getLedgerBalanceSmallestUnit().longValue()), l.getNativeCcySymbol());
                list.add(new ValidationResult(ValidationState.Error, String.format("Sum of %s payments from %s is %s and exeeds wallet balance of %s.", affectedPayments.size(), w.getPublicKey(), sumNativeText, balanceNativeText)));
            }

            if (StringUtils.isAllEmpty(w.getSecret())) {
                list.add(new ValidationResult(ValidationState.Error, "Sender wallet secret (private Key) is missing."));
            }
        }

        return list.toArray(new ValidationResult[0]);
    }

    private ArrayList<Wallet> getSendingWalletsOf(Payment[] payments) {
        var list = new ArrayList<Wallet>();
        for (var p : payments) {
            var existing = list.stream().anyMatch(w -> isSame(p.getSenderWallet(), w));
            if (!existing) {
                list.add(p.getSenderWallet());
            }
        }
        return list;
    }

    private boolean isSame(Wallet first, Wallet second) {
        return first.getPublicKey().equals(second.getPublicKey());
    }

    private ArrayList<Payment> filterSendingWallet(Wallet w, Payment[] payments) {
        var list = new ArrayList<Payment>();
        for (var p : payments) {
            if (isSame(p.getSenderWallet(), w)) {
                list.add(p);
            }
        }
        return list;
    }
}
