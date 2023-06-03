package com.radynamics.dallipay.cryptoledger.ethereum.paymentpath;

import com.radynamics.dallipay.cryptoledger.PaymentPath;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;
import org.apache.commons.lang3.NotImplementedException;

public class Erc20Path implements PaymentPath {
    private final Currency ccy;

    private final static int maxRank = 10;

    public Erc20Path(Currency ccy) {
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        if (ccy.getIssuer() == null) throw new IllegalArgumentException("Parameter 'ccy.getIssuer()' cannot be null");
        this.ccy = ccy;
    }

    @Override
    public int getRank() {
        return maxRank;
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
        return ccy.getCode();
    }

    @Override
    public boolean isVolatile() {
        return false;
    }

    @Override
    public Currency getCcy() {
        return ccy;
    }
}
