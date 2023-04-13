package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.cryptoledger.LedgerException;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.xrpl.Ledger;
import com.radynamics.dallipay.cryptoledger.generic.Wallet;
import com.radynamics.dallipay.cryptoledger.xrpl.api.PaymentBuilder;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Utils;
import com.radynamics.dallipay.iso20022.pain001.TestTransaction;
import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

public class PayloadConverterTest {
    private static final Currency eur = new Currency("EUR", new Wallet("rKYNzaJ3UZjHSENVgiu9ULKARvF2CuS8xg"));
    private static final Ledger ledger = new Ledger();

    @Test
    public void toJsonParamNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> PayloadConverter.toJson(null));
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "123456"}, nullValues = {"null"})
    public void toJsonDestinationTag(Integer destinationTag) throws LedgerException {
        var t = createTransaction();
        var builder = PaymentBuilder.builder().payment(t).build();
        if (destinationTag != null) {
            builder.destinationTag(UnsignedInteger.valueOf(destinationTag));
        }
        var json = PayloadConverter.toJson(builder.build());

        Assertions.assertEquals("Payment", json.getString("TransactionType"));
        Assertions.assertEquals("rhEo7YkHrxMzqwPhCASpeNwL2HNMqfsb87", json.getString("Account"));
        Assertions.assertEquals("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", json.getString("Destination"));
        if (destinationTag == null) {
            Assertions.assertNull(json.optJSONObject("DestinationTag"));
        } else {
            Assertions.assertEquals(destinationTag, json.getInt("DestinationTag"));
        }
        Assertions.assertEquals(15, json.getLong("Fee"));
        Assertions.assertEquals(100000000, json.getLong("Amount"));
        Assertions.assertNull(json.optJSONObject("SendMax"));
        Assertions.assertNull(json.optJSONObject("Memos"));
    }

    @Test
    public void toJsonAmountIssuedCurrency() throws LedgerException {
        var t = createTransaction();
        t.setAmount(Money.of(1234.56, eur));
        var builder = PaymentBuilder.builder().payment(t).build();
        var json = PayloadConverter.toJson(builder.build());

        var amt = json.optJSONObject("Amount");
        Assertions.assertNotNull(amt);
        Assertions.assertEquals("1234.56", amt.getString("value"));
        Assertions.assertEquals(eur.getCode(), amt.getString("currency"));
        Assertions.assertEquals(eur.getIssuer().getPublicKey(), amt.getString("issuer"));
    }

    @ParameterizedTest
    @CsvSource(value = {"0.0001,XRP", "0.15,EUR"})
    public void toJsonSendMax(Double amount, String ccy) throws LedgerException {
        var t = createTransaction();
        var builder = PaymentBuilder.builder().payment(t).build();

        var isNativeCcyAmount = ccy.equals(ledger.getNativeCcySymbol());
        if (isNativeCcyAmount) {
            builder.sendMax(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(amount)));
        } else {
            var issuedAmount = IssuedCurrencyAmount.builder().value(String.valueOf(amount)).currency(ccy).issuer(Address.of(eur.getIssuer().getPublicKey())).build();
            builder.sendMax(issuedAmount);
        }
        var json = PayloadConverter.toJson(builder.build());

        if (isNativeCcyAmount) {
            Assertions.assertEquals("100", json.getString("SendMax"));
        } else {
            var amt = json.optJSONObject("SendMax");
            Assertions.assertNotNull(amt);
            Assertions.assertEquals("0.15", amt.getString("value"));
            Assertions.assertEquals(eur.getCode(), amt.getString("currency"));
            Assertions.assertEquals(eur.getIssuer().getPublicKey(), amt.getString("issuer"));
        }
    }

    @Test
    public void toJsonMemo() throws LedgerException, DecoderException, UnsupportedEncodingException {
        var t = createTransaction();
        t.addMessage("Test 1");
        t.addMessage("Test 2");
        var builder = PaymentBuilder.builder().payment(t).build();
        var json = PayloadConverter.toJson(builder.build());

        var memos = json.optJSONArray("Memos");
        Assertions.assertNotNull(memos);
        Assertions.assertEquals(1, memos.length());
        var memo = memos.getJSONObject(0);
        var memoData = memo.optJSONObject("Memo");
        Assertions.assertNotNull(memoData);
        Assertions.assertEquals("{\"CdOrPrtry\":[],\"v\":1,\"ft\":[\"Test 1\",\"Test 2\"]}", Utils.hexToString(memoData.getString("MemoData")));
    }

    private static Transaction createTransaction() {
        var ccy = new Currency(ledger.getNativeCcySymbol());
        var t = new TestTransaction(ledger, Money.of(100d, ccy));
        t.setSenderWallet(ledger.createWallet("rhEo7YkHrxMzqwPhCASpeNwL2HNMqfsb87", null));
        t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
        t.setInvoiceId("RG-00123.45");
        t.setLedgerTransactionFee(Money.of(0.000015, ccy));
        return t;
    }
}
