package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.paymentpath;

import com.radynamics.CryptoIso20022Interop.cryptoledger.PaymentPath;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyFormatter;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public class IssuedCurrencyPath implements PaymentPath {
    private final CurrencyFormatter currencyFormatter;
    private final Currency ccy;
    private final int rankDeduction;

    public IssuedCurrencyPath(CurrencyFormatter currencyFormatter, Currency ccy, int rankDeduction) {
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
        return 10 - Math.max(rankDeduction, 0);
    }

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
    public String toString() {
        return String.format("Ccy: %s", ccy.toString());
    }
}
