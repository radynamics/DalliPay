package com.radynamics.dallipay.browserwalletbridge.gemwallet;

import com.radynamics.dallipay.cryptoledger.Transaction;
import org.json.JSONObject;

public class PayloadConverter implements com.radynamics.dallipay.browserwalletbridge.PayloadConverter {
    public JSONObject toJson(Transaction t) {
        if (t == null) throw new IllegalArgumentException("Parameter 't' cannot be null");

        var json = new JSONObject();
        json.put("amount", t.getAmount().getNumber().doubleValue());
        var ccy = t.getAmount().getCcy();
        if (!ccy.getCode().equals(t.getLedger().getNativeCcySymbol())) {
            json.put("currency", ccy.getCode());
            json.put("issuer", ccy.getIssuer().getPublicKey());
        }
        json.put("destination", t.getReceiverWallet().getPublicKey());

        // TODO: Memos

        return json;
    }
}
