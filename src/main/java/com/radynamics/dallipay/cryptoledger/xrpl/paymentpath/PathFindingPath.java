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
    private final int rankDeduction;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Xrpl");

    public PathFindingPath(CurrencyFormatter currencyFormatter, Currency ccy, int rankDeduction) {
        if (currencyFormatter == null) throw new IllegalArgumentException("Parameter 'currencyFormatter' cannot be null");
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        if (ccy.getIssuer() == null) throw new IllegalArgumentException("Parameter 'ccy.getIssuer()' cannot be null");
        if (rankDeduction < 0) throw new IllegalArgumentException("Parameter 'rankDeduction' cannot be less than 0");
        this.currencyFormatter = currencyFormatter;
        this.ccy = ccy;
        this.rankDeduction = rankDeduction;
    }

    @Override
    public int getRank() {
        return 5 - Math.max(rankDeduction, 0);
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
