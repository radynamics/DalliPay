package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.cryptoledger.bitcoin.api.ApiException;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FeeRefresher {
    private final Payment[] payments;
    private final Map<String, FeeSuggestion> feeCache = new HashMap<>();

    public FeeRefresher(Payment[] payments) {
        this.payments = payments;
    }

    public void refresh() throws ApiException {
        feeCache.clear();
        for (var p : payments) {
            if (p.getFeeSuggestion() == null) {
                p.setFeeSuggestion(getOrLoadFeeSuggestion(p));
            }
            p.setLedgerTransactionFee(p.getFeeSuggestion().getLow());
        }
    }

    public void setAllLow() {
        setAll(FeeSuggestion::getLow);
    }

    public void setAllMedium() {
        setAll(FeeSuggestion::getMedium);
    }

    public void setAllHigh() {
        setAll(FeeSuggestion::getHigh);
    }

    public void setAllCustom(Money amount) {
        setAll(feeSuggestion -> amount);
    }

    private void setAll(Function<FeeSuggestion, Money> getFee) {
        for (var p : payments) {
            p.setLedgerTransactionFee(getFee.apply(p.getFeeSuggestion()));
        }
    }

    public boolean allLow() {
        return all(FeeSuggestion::getLow);
    }

    public boolean allMedium() {
        return all(FeeSuggestion::getMedium);
    }

    public boolean allHigh() {
        return all(FeeSuggestion::getHigh);
    }

    public boolean custom() {
        return !allLow() && !allMedium() && !allHigh();
    }

    private boolean all(Function<FeeSuggestion, Money> getFee) {
        for (var p : payments) {
            var ledgerTransactionFee = FeeHelper.get(p.getFees(), FeeType.LedgerTransactionFee).orElseThrow();
            if (!getFee.apply(p.getFeeSuggestion()).equals(ledgerTransactionFee)) {
                return false;
            }
        }
        return true;
    }

    private FeeSuggestion getOrLoadFeeSuggestion(Payment p) throws ApiException {
        var ledger = p.getLedger();
        if (!ledger.equalTransactionFees()) {
            return ledger.getFeeSuggestion(p.getTransaction());
        }

        // Transaction fees remain equal irrelevant of content.
        var key = createKey(p);
        if (!feeCache.containsKey(key)) {
            feeCache.put(key, ledger.getFeeSuggestion(p.getTransaction()));
        }
        return feeCache.get(key);
    }

    private static final String createKey(Payment p) {
        return p.getLedger().getId().textId();
    }

    public FeeSuggestion firstFeeSuggestion() throws ApiException {
        return payments.length == 0 ? null : getOrLoadFeeSuggestion(payments[0]);
    }
}
