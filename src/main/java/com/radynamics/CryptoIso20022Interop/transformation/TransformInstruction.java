package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.Config;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.DateFormat;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// This class contains all information to convert payments from pain001 into target ledger payments.
public class TransformInstruction {
    final static Logger log = LogManager.getLogger(TransformInstruction.class);

    private Ledger ledger;
    private final Config config;
    private final AccountMappingSource accountMappingSource;
    private ExchangeRateProvider exchangeRateProvider;

    private String senderPublicKey;
    private String senderSecret;
    private String targetCcy = "USD";
    private DateFormat bookingDateFormat = DateFormat.DateTime;
    private DateFormat valutaDateFormat = DateFormat.DateTime;
    private StructuredReference creditorReferenceIfMissing;
    private ExchangeRateProvider historicExchangeRateSource;

    public TransformInstruction(Ledger ledger, Config config, AccountMappingSource accountMappingSource) {
        this.ledger = ledger;
        this.config = config;
        this.accountMappingSource = accountMappingSource;
    }

    public Wallet getWalletOrNull(Account account) {
        if (account == null) {
            return null;
        }
        var wallet = accountMappingSource.getWalletOrNull(account);
        if (wallet == null) {
            return null;
        }
        wallet.setSecret(wallet.getPublicKey().equals(senderPublicKey) ? senderSecret : null);
        return wallet;
    }

    public Account getAccountOrNull(Wallet wallet) {
        return wallet == null ? null : accountMappingSource.getAccountOrNull(wallet);
    }

    public void setStaticSender(String publicKey, String secret) {
        this.senderPublicKey = publicKey;
        this.senderSecret = secret;
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

    public Network getNetwork() {
        return ledger.getNetwork().getType();
    }

    public void setNetwork(Network network) {
        ledger.setNetwork(config.getNetwork(network));
    }
}
