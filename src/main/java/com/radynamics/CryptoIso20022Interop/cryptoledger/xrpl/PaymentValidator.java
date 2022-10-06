package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.WalletHistoryValidator;

import java.util.ArrayList;

public class PaymentValidator implements com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator {
    private final TrustlineCache trustlineCache;

    public PaymentValidator(TrustlineCache cache) {
        if (cache == null) throw new IllegalArgumentException("Parameter 'cache' cannot be null");
        this.trustlineCache = cache;
    }

    @Override
    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();

        if (t.getReceiverWallet() != null) {
            list.addAll(validateTrustlineLimit(t.getReceiverWallet(), t.getAmountTransaction()));
        }

        return list.toArray(new ValidationResult[0]);
    }

    private ArrayList<ValidationResult> validateTrustlineLimit(Wallet wallet, Money sendingAmt) {
        var list = new ArrayList<ValidationResult>();

        for (var t : trustlineCache.get(WalletConverter.from(wallet))) {
            var ccy = sendingAmt.getCcy();
            if (!t.getBalance().getCcy().equals(ccy)) {
                continue;
            }

            var current = wallet.getBalances().get(ccy).orElseThrow();
            var afterwards = current.plus(sendingAmt);
            if (t.getLimit().lessThan(afterwards)) {
                list.add(new ValidationResult(ValidationState.Error, String.format("Receiver's limit for %s would be exceeded with this payment. Receiver must increase limit first.", ccy.getCode())));
            }
        }
        return list;
    }

    @Override
    public WalletHistoryValidator getHistoryValidator() {
        return null;
    }

    @Override
    public void clearCache() {
        trustlineCache.clear();
    }
}
