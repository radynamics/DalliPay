package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.db.AccountMappingRepo;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.DateFormat;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

// This class contains all information to convert payments from pain001 into target ledger payments.
public class TransformInstruction {
    final static Logger log = LogManager.getLogger(TransformInstruction.class);

    private Ledger ledger;
    private ExchangeRateProvider exchangeRateProvider;
    private ArrayList<AccountMapping> accountMappings = new ArrayList<>();

    private String senderPublicKey;
    private String senderSecret;
    private String targetCcy = "USD";
    private DateFormat bookingDateFormat = DateFormat.DateTime;
    private DateFormat valutaDateFormat = DateFormat.DateTime;
    private StructuredReference creditorReferenceIfMissing;
    private ExchangeRateProvider historicExchangeRateSource;

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
        try (var repo = new AccountMappingRepo()) {
            var found = repo.list(ledger.getId(), account);
            if (found.length == 0) {
                return null;
            }
            var wallet = found[0].getWallet();
            wallet.setSecret(wallet.getPublicKey().equals(senderPublicKey) ? senderSecret : null);
            return wallet;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public IbanAccount getIbanOrNull(Wallet wallet) {
        return (IbanAccount) getAccountOrNull(wallet);
    }

    public Account getAccountOrNull(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        try (var repo = new AccountMappingRepo()) {
            var found = repo.list(ledger.getId(), wallet);
            return found.length == 0 ? null : found[0].getAccount();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
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
}
