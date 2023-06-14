package com.radynamics.dallipay.browserwalletbridge.metamask;

import com.radynamics.dallipay.cryptoledger.Transaction;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PayloadConverter implements com.radynamics.dallipay.browserwalletbridge.PayloadConverter {
    public JSONObject toJson(Transaction t) {
        if (t == null) throw new IllegalArgumentException("Parameter 't' cannot be null");

        var json = new JSONObject();
        var ccy = t.getAmount().getCcy();
        if (!ccy.getCode().equals(t.getLedger().getNativeCcySymbol())) {
            json.put("issuer", ccy.getIssuer().getPublicKey());
        }
        json.put("amount", toWei(t.getAmount().getNumber().doubleValue()));
        json.put("destination", t.getReceiverWallet().getPublicKey());

        // TODO: Memos

        return json;
    }

    private BigInteger toWei(Double amount) {
        var factor = BigDecimal.TEN.pow(18);
        return BigDecimal.valueOf(amount).multiply(factor).toBigInteger();
    }
}
