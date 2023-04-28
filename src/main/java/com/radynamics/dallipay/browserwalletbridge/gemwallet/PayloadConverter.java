package com.radynamics.dallipay.browserwalletbridge.gemwallet;

import com.radynamics.dallipay.cryptoledger.xrpl.Transaction;
import org.json.JSONObject;

public final class PayloadConverter {
    public static JSONObject toJson(Transaction payment) {
        if (payment == null) throw new IllegalArgumentException("Parameter 'payment' cannot be null");

        var json = new JSONObject();
        json.put("amount", payment.getAmount().getNumber());
        var ccy = payment.getAmount().getCcy();
        if (!ccy.getCode().equals(payment.getLedger().getNativeCcySymbol())) {
            json.put("currency", ccy.getCode());
            json.put("issuer", ccy.getIssuer().getPublicKey());
        }
        json.put("destination", payment.getReceiverWallet().getPublicKey());

        // TODO: Memos

        return json;
    }
}
