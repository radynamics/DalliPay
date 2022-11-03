package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletCompare;

import java.util.Objects;

public class Currency {
    private final String code;
    private final Wallet issuer;
    private Double transferFee = (double) 0;

    public Currency(String ccy) {
        this(ccy, null);
    }

    public Currency(String ccy, Wallet issuer) {
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        if (ccy.length() == 0) throw new IllegalArgumentException("Parameter 'ccy' cannot be empty");
        this.code = ccy;
        this.issuer = issuer;
    }

    public boolean sameCode(Currency ccy) {
        if (ccy == null) return false;
        return code.equals(ccy.getCode());
    }

    public Currency withoutIssuer() {
        return new Currency(code);
    }

    public String getCode() {
        return code;
    }

    public Wallet getIssuer() {
        return issuer;
    }

    public void setTransferFee(double transferFee) {
        this.transferFee = transferFee;
    }

    /**
     * Transfer fee as percentage value between 0 (0%, no fee) and 1 (100%) of a payment amount.
     */
    public Double getTransferFee() {
        return transferFee;
    }

    /**
     * Transfer fee as absolute amount value for a given amount to send.
     */
    public Money getTransferFeeAmount(Money sendingAmt) {
        if (!equals(sendingAmt.getCcy())) throw new IllegalArgumentException(String.format("Parameter 'sendingAmt' must have ccy '%s'", this));
        return sendingAmt.multiply(getTransferFee());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency = (Currency) o;
        return Objects.equals(code, currency.code) && WalletCompare.isSame(issuer, currency.issuer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, issuer);
    }

    @Override
    public String toString() {
        return issuer == null ? code : String.format("%s (%s)", code, issuer.getPublicKey());
    }
}
