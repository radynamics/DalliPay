package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.Config;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.xrpl.XrplPriceOracleConfig;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.iso20022.camt054.DateFormat;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// This class contains all information to convert payments from pain001 into target ledger payments.
public class TransformInstruction {
    final static Logger log = LogManager.getLogger(TransformInstruction.class);

    private Ledger ledger;
    private final Config config;
    private final AccountMappingSource accountMappingSource;
    private ExchangeRateProvider exchangeRateProvider;

    private String targetCcy = XrplPriceOracleConfig.AsReceived;
    private DateFormat bookingDateFormat = DateFormat.DateTime;
    private DateFormat valutaDateFormat = DateFormat.DateTime;
    private StructuredReference creditorReferenceIfMissing;
    private ExchangeRateProvider historicExchangeRateSource;

    public TransformInstruction(Ledger ledger, Config config, AccountMappingSource accountMappingSource) {
        this.ledger = ledger;
        this.config = config;
        this.accountMappingSource = accountMappingSource;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public ExchangeRateProvider getExchangeRateProvider() {
        return exchangeRateProvider;
    }

    public void setExchangeRateProvider(ExchangeRateProvider exchangeRateProvider) {
        this.exchangeRateProvider = exchangeRateProvider;
    }

    public String getTargetCcy() {
        return this.targetCcy;
    }

    public void setTargetCcy(String targetCcy) {
        if (targetCcy == null) throw new IllegalArgumentException("Parameter 'targetCcy' cannot be null");
        // If set to another currency than ledger's native currency, amounts are converted using rates provided by exchange.
        this.targetCcy = targetCcy;
    }

    public DateFormat getBookingDateFormat() {
        return bookingDateFormat;
    }

    public void setBookingDateFormat(DateFormat bookingDateFormat) {
        this.bookingDateFormat = bookingDateFormat;
    }

    public DateFormat getValutaDateFormat() {
        return valutaDateFormat;
    }

    public void setValutaDateFormat(DateFormat valutaDateFormat) {
        this.valutaDateFormat = valutaDateFormat;
    }

    public StructuredReference getCreditorReferenceIfMissing() {
        return creditorReferenceIfMissing;
    }

    public void setCreditorReferenceIfMissing(StructuredReference creditorReferenceIfMissing) {
        this.creditorReferenceIfMissing = creditorReferenceIfMissing;
    }

    public ExchangeRateProvider getHistoricExchangeRateSource() {
        return historicExchangeRateSource;
    }

    public void setHistoricExchangeRateSource(ExchangeRateProvider historicExchangeRateSource) {
        this.historicExchangeRateSource = historicExchangeRateSource;
    }

    public AccountMappingSource getAccountMappingSource() {
        return this.accountMappingSource;
    }

    public NetworkInfo getNetwork() {
        return ledger.getNetwork();
    }

    public void setNetwork(NetworkInfo network) {
        ledger.setNetwork(network);
    }

    public Config getConfig() {
        return config;
    }
}
