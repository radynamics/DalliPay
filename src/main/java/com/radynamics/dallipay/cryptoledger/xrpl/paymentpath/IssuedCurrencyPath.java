package com.radynamics.dallipay.cryptoledger.xrpl.paymentpath;

import com.radynamics.dallipay.cryptoledger.PaymentPath;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyFormatter;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;

public class IssuedCurrencyPath implements PaymentPath {
    private final CurrencyFormatter currencyFormatter;
    private final Currency ccy;
    private final Double transferFee;

    private final static int maxRank = 10;

    public IssuedCurrencyPath(CurrencyFormatter currencyFormatter, Currency ccy, double transferFee) {
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
        return maxRank - getRankDeduction();
    }

    private int getRankDeduction() {
        if (transferFee == 0) return 0;
        if (0 < transferFee && transferFee <= 0.001) return 1;
        if (0.001 < transferFee && transferFee <= 0.002) return 2;
        if (0.002 < transferFee && transferFee <= 0.005) return 3;
        if (0.005 < transferFee && transferFee <= 0.010) return 4;
        if (0.010 < transferFee && transferFee <= 0.020) return 5;
        if (0.020 < transferFee && transferFee <= 0.050) return 6;
        return maxRank;
    }

    @Override
    public Currency getCcy() {
        return ccy;
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
        var sb = new StringBuilder();
        sb.append(currencyFormatter.formatCcyAndIssuer(ccy));
        if (ccy.getTransferFee() != 0) {
            sb.append(String.format(", %s", currencyFormatter.formatTransferFee(ccy)));
        }
        return sb.toString();
    }

    @Override
    public boolean isVolatile() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("Ccy: %s", ccy.toString());
    }
}
