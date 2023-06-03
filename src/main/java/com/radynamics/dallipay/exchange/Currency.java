package com.radynamics.dallipay.exchange;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletCompare;

import java.util.Objects;

public class Currency {
    private final String code;
    private final String ledgerCode;
    private final Wallet issuer;
    private Double transferFee = (double) 0;

    public Currency(String ccy) {
        this(ccy, ccy, null);
    }

    public Currency(String ccy, Wallet issuer) {
        this(ccy, ccy, issuer);
    }

    public Currency(String ccy, String ledgerCcy, Wallet issuer) {
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        if (ccy.length() == 0) throw new IllegalArgumentException("Parameter 'ccy' cannot be empty");
        if (ledgerCcy == null) throw new IllegalArgumentException("Parameter 'ledgerCcy' cannot be null");
        if (ledgerCcy.length() == 0) throw new IllegalArgumentException("Parameter 'ledgerCcy' cannot be empty");
        this.code = ccy;
        this.ledgerCode = ledgerCcy;
        this.issuer = issuer;
    }

    public boolean sameCode(Currency ccy) {
        if (ccy == null) return false;
        return code.equals(ccy.getCode()) && ledgerCode.equals(ccy.getLedgerCode());
    }

    public Currency withoutIssuer() {
        return new Currency(code, ledgerCode, null);
    }

    public String getCode() {
        return code;
    }

    public String getLedgerCode() {
        return ledgerCode;
    }

    public Wallet getIssuer() {
        return issuer;
    }

    public void setTransferFee(double transferFee) {
        if (transferFee < 0 || transferFee > 1) throw new IllegalArgumentException("Parameter 'transferFee' must be between 0 and 1");
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
        return Objects.equals(code, currency.code) && Objects.equals(ledgerCode, currency.ledgerCode) && WalletCompare.isSame(issuer, currency.issuer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, ledgerCode, issuer);
    }

    @Override
    public String toString() {
        return issuer == null ? code : String.format("%s;%s (%s)", code, ledgerCode, issuer.getPublicKey());
    }
}
