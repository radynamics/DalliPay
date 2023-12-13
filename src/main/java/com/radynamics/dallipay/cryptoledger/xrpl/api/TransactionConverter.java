package com.radynamics.dallipay.cryptoledger.xrpl.api;

import com.radynamics.dallipay.cryptoledger.generic.Transaction;
import com.radynamics.dallipay.cryptoledger.generic.Wallet;
import com.radynamics.dallipay.cryptoledger.memo.PayloadConverter;
import com.radynamics.dallipay.cryptoledger.xrpl.Ledger;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Utils;
import org.apache.commons.codec.DecoderException;
import org.xrpl.xrpl4j.model.transactions.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TransactionConverter {
    private final Ledger ledger;

    public TransactionConverter(Ledger ledger) {
        this.ledger = ledger;
    }

    Transaction toTransaction(Payment p, CurrencyAmount deliveredAmount) {
        var future = new CompletableFuture<Transaction>();
        deliveredAmount.handle(xrpCurrencyAmount -> {
            try {
                future.complete(toTransaction(p, xrpCurrencyAmount));
            } catch (DecoderException | UnsupportedEncodingException e) {
                future.completeExceptionally(e);
            }
        }, issuedCurrencyAmount -> {
            try {
                future.complete(toTransaction(p, issuedCurrencyAmount));
            } catch (ExecutionException | InterruptedException | DecoderException | UnsupportedEncodingException e) {
                future.completeExceptionally(e);
            }
        });
        return future.join();
    }

    private Transaction toTransaction(org.xrpl.xrpl4j.model.transactions.Payment p, IssuedCurrencyAmount amount) throws ExecutionException, InterruptedException, DecoderException, UnsupportedEncodingException {
        var ccyCode = Convert.toCurrencyCode(amount.currency());
        var amt = BigDecimal.valueOf(Double.parseDouble(amount.value()));

        var issuer = ledger.createWallet(amount.issuer().value(), "");
        var ccy = new Currency(ccyCode, issuer);
        // When the issuer field of the destination Amount field matches the Destination address, it is treated as a special case meaning "any issuer that the destination accepts." (https://xrpl.org/payment.html)
        if (!issuer.getPublicKey().equals((p.destination().value())) || p.sendMax().isEmpty()) {
            return toTransaction(p, amt, ccy);
        }

        var future = new CompletableFuture<Transaction>();
        p.sendMax().get().handle(xrpCurrencyAmount -> {
                    try {
                        future.complete(toTransaction(p, amt, ccy));
                    } catch (DecoderException | UnsupportedEncodingException e) {
                        future.completeExceptionally(e);
                    }
                },
                issuedCurrencyAmountSendMax -> {
                    try {
                        var issuerSendMax = ledger.createWallet(issuedCurrencyAmountSendMax.issuer().value(), "");
                        future.complete(toTransaction(p, amt, new Currency(ccyCode, issuerSendMax)));
                    } catch (DecoderException | UnsupportedEncodingException e) {
                        future.completeExceptionally(e);
                    }
                });

        return future.join();
    }

    private Transaction toTransaction(org.xrpl.xrpl4j.model.transactions.Payment p, XrpCurrencyAmount deliveredAmount) throws DecoderException, UnsupportedEncodingException {
        return toTransaction((org.xrpl.xrpl4j.model.transactions.Transaction) p, deliveredAmount);
    }

    Transaction toTransaction(org.xrpl.xrpl4j.model.transactions.Transaction t, XrpCurrencyAmount deliveredAmount) throws DecoderException, UnsupportedEncodingException {
        return toTransaction(t, deliveredAmount.toXrp(), new Currency(ledger.getNativeCcySymbol()));
    }

    private Transaction toTransaction(org.xrpl.xrpl4j.model.transactions.Transaction t, BigDecimal amt, Currency ccy) throws DecoderException, UnsupportedEncodingException {
        var trx = new Transaction(ledger, Money.of(amt.doubleValue(), ccy));
        trx.setId(t.hash().get().value());
        trx.setBooked(t.closeDateHuman().get());
        trx.setBlock(new LedgerBlock(t.ledgerIndex().orElseThrow()));
        trx.setSender(from(t.account()));
        var messages = new ArrayList<String>();
        for (MemoWrapper mw : t.memos()) {
            if (!mw.memo().memoData().isPresent()) {
                continue;
            }
            var unwrappedMemo = PayloadConverter.fromMemo(Utils.hexToString(mw.memo().memoData().get()));
            messages.addAll(Arrays.asList(unwrappedMemo.freeTexts()));
        }

        var l = new StructuredReferenceLookup(t);
        for (var r : l.find()) {
            trx.addStructuredReference(r);
            messages.removeIf(o -> o.equals(r.getUnformatted()));
        }
        for (var m : messages) {
            trx.addMessage(m);
        }

        if (t.transactionType() == TransactionType.PAYMENT) {
            var p = (Payment) t;
            trx.setReceiver(from(p.destination()));
            if (p.destinationTag().isPresent()) {
                trx.setDestinationTag(p.destinationTag().get().toString());
            }
            trx.setInvoiceId(p.invoiceId().isEmpty() ? "" : p.invoiceId().get().value());
        }

        return trx;
    }

    private Wallet from(Address address) {
        return new Wallet(ledger.getId(), address.value());
    }
}
