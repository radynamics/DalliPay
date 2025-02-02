package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerFactory;
import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.transaction.Origin;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReferenceFactory;
import com.radynamics.dallipay.util.QueryParam;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

public class PaymentRequestUri {
    private final LedgerId ledgerId;
    private final String ledgerNativeCcy;
    private final URI uri;

    private final Money amount;
    private final String destinationTag;
    private final String ccy;
    private final String refNo;
    private final String msg;
    private final NetworkInfo expectedNetwork;

    private PaymentRequestUri(Ledger defaultLedger, URI uri) {
        this.uri = uri;

        destinationTag = firstOrNull(uri, "dt");
        ccy = firstOrNull(uri, "currency");
        refNo = firstOrNull(uri, "refno");
        msg = firstOrNull(uri, "msg");

        Ledger tmpLedger = defaultLedger;
        NetworkInfo tmpNetwork = defaultLedger.getNetwork();
        var networkId = getNetworkIdOrNull();
        if (networkId != null && !Objects.equals(tmpNetwork.getNetworkId(), networkId)) {
            for (var l : LedgerFactory.all()) {
                for (var n : l.getDefaultNetworkInfo()) {
                    if (n.getNetworkId().equals(networkId)) {
                        tmpLedger = l;
                        tmpNetwork = n;
                    }
                }
            }
        }

        // Don't store ledger reference due temporary instances aren't initialized (various fields null).
        ledgerId = tmpLedger.getId();
        ledgerNativeCcy = tmpLedger.getNativeCcySymbol();
        expectedNetwork = tmpNetwork;
        amount = parseAmount();
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

    public static PaymentRequestUri create(Ledger ledger, URI uri) throws Exception {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        if (uri == null) throw new IllegalArgumentException("Parameter 'uri' cannot be null");

        if (!matches(uri)) {
            throw new Exception("Not a valid payment request URI. " + uri);
        }

        return new PaymentRequestUri(ledger, uri);
    }

    public LedgerId ledgerId() {
        return ledgerId;
    }

    public NetworkInfo networkInfo() {
        return expectedNetwork;
    }

    public Payment create(Ledger ledger) {
        var payment = new Payment(ledger.createTransaction());
        payment.setAmount(amount != null ? amount : Money.zero(new Currency(ledger.getNativeCcySymbol())));
        payment.setReceiverWallet(ledger.createWallet(getTo(uri), null));
        payment.setOrigin(Origin.Manual);

        if (!StringUtils.isEmpty(destinationTag)) {
            payment.setDestinationTag(destinationTag);
        }

        if (!StringUtils.isEmpty(ccy)) {
            payment.setUserCcy(new Currency(ccy.toUpperCase(Locale.ROOT)));
        }

        if (!StringUtils.isEmpty(refNo)) {
            payment.addStructuredReference(StructuredReferenceFactory.create(StructuredReferenceFactory.detectType(refNo), refNo));
        }

        if (!StringUtils.isEmpty(msg)) {
            payment.addMessage(msg);
        }

        return payment;
    }

    private Money parseAmount() {
        var amount = getAmountOrNull();
        if (amount == null) {
            return null;
        }

        var ccy = firstOrNull(uri, "currency");
        if (StringUtils.isEmpty(ccy)) {
            ccy = ledgerNativeCcy;
        }

        return Money.of(amount, new Currency(ccy));
    }

    private Double getAmountOrNull() {
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

    private Integer getNetworkIdOrNull() {
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
