package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerFactory;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.transaction.Origin;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReferenceFactory;
import com.radynamics.dallipay.util.QueryParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Locale;

public class PaymentRequestUri {
    private final static Logger log = LogManager.getLogger(PaymentRequestUri.class);
    private final Ledger ledger;

    public PaymentRequestUri(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
    }

    public static boolean matches(String text) {
        if (text == null) {
            return false;
        }

        try {
            return matches(URI.create(text));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean matches(URI uri) {
        var receiverWallet = getTo(uri);
        if (receiverWallet == null || receiverWallet.length() == 0) {
            return false;
        }

        return true;
    }

    public Ledger ledger(URI uri) {
        var networkId = getNetworkIdOrNull(uri);
        if (networkId == null) {
            return null;
        }

        for (var l : LedgerFactory.all()) {
            for (var n : l.getDefaultNetworkInfo()) {
                if (n.getNetworkId().equals(networkId)) {
                    return l;
                }
            }
        }

        return null;
    }

    public NetworkInfo networkInfo(URI uri) {
        var networkId = getNetworkIdOrNull(uri);
        if (networkId == null) {
            return null;
        }

        for (var l : LedgerFactory.all()) {
            for (var n : l.getDefaultNetworkInfo()) {
                if (n.getNetworkId().equals(networkId)) {
                    return n;
                }
            }
        }

        return null;
    }

    public Payment createOrNull(URI uri) {
        if (!matches(uri)) {
            return null;
        }

        var payment = new Payment(ledger.createTransaction());
        var amount = parseAmount(uri);
        payment.setAmount(amount != null ? amount : Money.zero(new Currency(ledger.getNativeCcySymbol())));
        payment.setReceiverWallet(ledger.createWallet(getTo(uri), null));
        payment.setOrigin(Origin.Manual);

        var destinationTag = firstOrNull(uri, "dt");
        if (!StringUtils.isEmpty(destinationTag)) {
            payment.setDestinationTag(destinationTag);
        }

        var ccy = firstOrNull(uri, "currency");
        if (!StringUtils.isEmpty(ccy)) {
            payment.setUserCcy(new Currency(ccy.toUpperCase(Locale.ROOT)));
        }

        var refNo = firstOrNull(uri, "refno");
        if (!StringUtils.isEmpty(refNo)) {
            payment.addStructuredReference(StructuredReferenceFactory.create(StructuredReferenceFactory.detectType(refNo), refNo));
        }

        var msg = firstOrNull(uri, "msg");
        if (!StringUtils.isEmpty(msg)) {
            payment.addMessage(msg);
        }

        return payment;
    }

    private Money parseAmount(URI uri) {
        var amount = getAmountOrNull(uri);
        if (amount == null) {
            return null;
        }

        var ccy = firstOrNull(uri, "currency");
        if (StringUtils.isEmpty(ccy)) {
            ccy = ledger.getNativeCcySymbol();
        }

        return Money.of(amount, new Currency(ccy));
    }

    private Double getAmountOrNull(URI uri) {
        var value = firstOrNull(uri, "amount");
        return isNumeric(value) ? Double.parseDouble(value) : null;
    }

    private static boolean isNumeric(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static String getTo(URI uri) {
        return firstOrNull(uri, "to");
    }

    private Integer getNetworkIdOrNull(URI uri) {
        if (!matches(uri)) {
            return null;
        }

        var networkIdText = firstOrNull(uri, "networkId");
        return StringUtils.isEmpty(networkIdText) || !isNumeric(networkIdText)
                ? null
                : Integer.parseInt(networkIdText);
    }

    private static String firstOrNull(URI uri, String param) {
        var params = QueryParam.split(uri.getQuery());
        return params.containsKey(param) ? params.get(param).get(0) : null;
    }
}
