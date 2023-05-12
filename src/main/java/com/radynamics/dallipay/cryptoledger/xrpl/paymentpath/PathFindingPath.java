package com.radynamics.dallipay.cryptoledger.xrpl.paymentpath;

import com.radynamics.dallipay.cryptoledger.PaymentPath;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyFormatter;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;

import java.util.ResourceBundle;

public class PathFindingPath implements PaymentPath {
    private final CurrencyFormatter currencyFormatter;
    private final Currency ccy;
    private final Double transferFee;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Xrpl");

    public PathFindingPath(CurrencyFormatter currencyFormatter, Currency ccy, Double transferFee) {
        if (currencyFormatter == null) throw new IllegalArgumentException("Parameter 'currencyFormatter' cannot be null");
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        if (ccy.getIssuer() == null) throw new IllegalArgumentException("Parameter 'ccy.getIssuer()' cannot be null");
        if (transferFee < 0 || transferFee > 1) throw new IllegalArgumentException("Parameter 'transferFee' must be between 0 and 1");
        this.currencyFormatter = currencyFormatter;
        this.ccy = ccy;
        this.transferFee = transferFee;
    }

    @Override
    public int getRank() {
        return 5 - getRankDeduction();
    }

    private int getRankDeduction() {
        if (transferFee == 0) return 0;
        if (0 < transferFee && transferFee <= 0.002) return 1;
        return 2;
    }

    @Override
    public void apply(Payment p) {
        p.setAmount(Money.of(p.getAmount(), ccy));
    }

    @Override
    public boolean isSet(Payment p) {
        return ccy.equals(p.getAmountTransaction().getCcy());
    }

    @Override
    public String getDisplayText() {
        return String.format("%s (%s)", currencyFormatter.formatCcyAndIssuer(ccy), res.getString("transient"));
    }

    @Override
    public Currency getCcy() {
        return ccy;
    }

    @Override
    public String toString() {
        return String.format("Ccy: %s", ccy.toString());
    }
}
