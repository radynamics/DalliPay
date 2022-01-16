package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.exchange.Exchange;
import com.radynamics.CryptoIso20022Interop.exchange.HistoricExchangeRateSource;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.DateFormat;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.util.ArrayList;

// This class contains all information to convert payments from pain001 into target ledger payments.
public class TransformInstruction {
    private Ledger ledger;
    private Exchange exchange;
    private ArrayList<AccountMapping> accountMappings = new ArrayList<>();

    private String senderPublicKey;
    private String senderSecret;
    private String targetCcy;
    private DateFormat bookingDateFormat = DateFormat.DateTime;
    private DateFormat valutaDateFormat = DateFormat.DateTime;
    private StructuredReference creditorReferenceIfMissing;
    private HistoricExchangeRateSource historicExchangeRateSource;

    public TransformInstruction(Ledger ledger) {
        this.ledger = ledger;
    }

    public void add(AccountMapping accountMapping) {
        accountMappings.add(accountMapping);
    }

    public Wallet getWalletOrNull(Account account) {
        if (account == null) {
            return null;
        }
        for (var item : accountMappings) {
            if (item.getAccount().getUnformatted().equalsIgnoreCase(account.getUnformatted())) {
                var secret = item.walletPublicKey.equals(senderPublicKey) ? senderSecret : null;
                return ledger.createWallet(item.walletPublicKey, secret);
            }
        }
        return null;
    }

    public IbanAccount getIbanOrNull(Wallet wallet) {
        return (IbanAccount) getAccountOrNull(wallet);
    }

    public Account getAccountOrNull(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        for (var item : accountMappings) {
            if (item.walletPublicKey.equalsIgnoreCase(wallet.getPublicKey())) {
                return item.getAccount();
            }
        }
        return null;
    }

    public void setStaticSender(String publicKey, String secret) {
        this.senderPublicKey = publicKey;
        this.senderSecret = secret;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public String getTargetCcy() {
        return this.targetCcy;
    }

    public void setTargetCcy(String targetCcy) {
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

    public HistoricExchangeRateSource getHistoricExchangeRateSource() {
        return historicExchangeRateSource;
    }

    public void setHistoricExchangeRateSource(HistoricExchangeRateSource historicExchangeRateSource) {
        this.historicExchangeRateSource = historicExchangeRateSource;
    }
}
