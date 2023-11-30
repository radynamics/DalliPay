package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.generic.WalletConverter;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationState;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.pain001.WalletHistoryValidator;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class PaymentValidator implements com.radynamics.dallipay.iso20022.PaymentValidator {
    private final Ledger ledger;
    private final TrustlineCache trustlineCache;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Validations");

    public PaymentValidator(Ledger ledger, TrustlineCache cache) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        if (cache == null) throw new IllegalArgumentException("Parameter 'cache' cannot be null");
        this.ledger = ledger;
        this.trustlineCache = cache;
    }

    @Override
    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();

        if (t.getLedger().createWalletValidator().isValidFormat(t.getReceiverWallet())) {
            list.addAll(validateTrustlineLimit(t.getReceiverWallet(), t.getAmountTransaction()));

            var ccy = t.getAmountTransaction().getCcy();
            if (ledger.exists(t.getReceiverWallet()) && !walletAccepts(t.getReceiverWallet(), ccy)) {
                list.add(new ValidationResult(ValidationState.Error, String.format(res.getString("receiverWalletDoesntAccept"), ccy.getCode())));
            }

            var xrplWallet = WalletConverter.from(t.getReceiverWallet());
            if (t.getDestinationTag() == null && ledger.requiresDestinationTag(xrplWallet)) {
                list.add(new ValidationResult(ValidationState.Error, res.getString("receiverWalletDestTag")));
            }
            var destTagBuilder = t.getLedger().createDestinationTagBuilder();
            if (!destTagBuilder.isValid(t.getDestinationTag())) {
                list.add(new ValidationResult(ValidationState.Error, String.format(res.getString("receiverWalletDestTagInvalid"), t.getDestinationTag())));
            }
            if (ledger.isBlackholed(xrplWallet)) {
                list.add(new ValidationResult(ValidationState.Error, res.getString("receiverWalletBlackholed")));
            }
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
                list.add(new ValidationResult(ValidationState.Error, String.format(res.getString("receiverTrustlineLimit"), ccy.getCode())));
            }
        }
        return list;
    }

    private boolean walletAccepts(Wallet wallet, Currency ccy) {
        if (ledger.getNativeCcySymbol().equals(ccy.getCode())) {
            return ledger.walletAcceptsXrp(wallet);
        }

        for (var t : trustlineCache.get(WalletConverter.from(wallet))) {
            if (t.getBalance().getCcy().equals(ccy)) {
                return true;
            }
        }

        return false;
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
