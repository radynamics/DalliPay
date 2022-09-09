package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import org.apache.commons.lang3.StringUtils;

public class PaymentCompare {
    public static final boolean isSimilar(Payment first, Payment second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null && second != null) {
            return false;
        }
        if (first != null && second == null) {
            return false;
        }

        if (!WalletCompare.isSame(first.getSenderWallet(), second.getSenderWallet())) {
            return false;
        }

        if (!WalletCompare.isSame(first.getReceiverWallet(), second.getReceiverWallet())) {
            return false;
        }

        if (!StringUtils.equals(first.getUserCcyCodeOrEmpty(), second.getUserCcyCodeOrEmpty())) {
            return false;
        }

        // Amount is unknown, if no exchange rate is applied (ex. fetched ledger transaction)
        if (!first.isAmountUnknown() && !second.isAmountUnknown()) {
            final Double tolerancePercent = 0.005;
            return Math.abs(first.getAmount() - second.getAmount()) <= first.getAmount() * tolerancePercent;
        } else {
            var sameCcy = StringUtils.equals(first.getAmountTransaction().getCcy().getCode(), second.getAmountTransaction().getCcy().getCode());
            final Double tolerancePercent = 0.02;
            return sameCcy && Math.abs(first.getAmountTransaction().minus(second.getAmountTransaction()).getNumber().doubleValue()) <= first.getAmountTransaction().multiply(tolerancePercent).getNumber().doubleValue();
        }
    }
}
