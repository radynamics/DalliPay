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

import java.util.ArrayList;
import java.util.Arrays;

public class PaymentValidator implements com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator {
    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();
        list.addAll(Arrays.asList(new Validator().validate(t)));

        var wv = new WalletValidator(t.getLedger());
        if (t.getReceiverWallet() != null) {
            var walletValidations = wv.validate(t.getReceiverWallet(), "Receiver");
            list.addAll(Arrays.asList(walletValidations));

            if (walletValidations.length == 0) {
                if (t.getLedger().requiresDestinationTag(t.getReceiverWallet())) {
                    list.add(new ValidationResult(ValidationState.Error, "Receiver wallet requires destination tag."));
                }
                if (!t.getLedger().walletAccepts(t.getReceiverWallet(), t.getLedgerCcy())) {
                    list.add(new ValidationResult(ValidationState.Error, String.format("Receiver wallet disallows receiving %s", t.getLedgerCcy())));
                }
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

        var sendingWallets = PaymentUtils.distinctSendingWallets(payments);
        for (var w : sendingWallets) {
            var affectedPayments = PaymentUtils.fromSender(w, payments);
            var sum = PaymentUtils.sumSmallestLedgerUnit(affectedPayments);
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
}
