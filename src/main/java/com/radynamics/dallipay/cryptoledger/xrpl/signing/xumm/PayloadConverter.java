package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.xrpl.XrplUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xrpl.xrpl4j.model.transactions.ImmutablePayment;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Memo;

public class PayloadConverter {
    static JSONObject toJson(ImmutablePayment payment, NetworkInfo networkInfo) {
        if (payment == null) throw new IllegalArgumentException("Parameter 'payment' cannot be null");
        var json = new JSONObject();

        json.put("TransactionType", "Payment");
        json.put("Account", payment.account().value());
        json.put("SourceTag", com.radynamics.dallipay.cryptoledger.xrpl.Ledger.APP_ID_TAG);
        json.put("Destination", payment.destination().value());
        if (payment.destinationTag().isPresent()) {
            json.put("DestinationTag", payment.destinationTag().get());
        }
        json.put("Fee", payment.fee().value());

        payment.amount().handle(
                xrpCurrencyAmount -> {
                    json.put("Amount", String.valueOf(xrpCurrencyAmount.value()));
                },
                issuedCurrencyAmount -> {
                    json.put("Amount", toAmount(issuedCurrencyAmount));
                });

        if (payment.sendMax().isPresent()) {
            payment.sendMax().get().handle(
                    xrpCurrencyAmount -> {
                        json.put("SendMax", String.valueOf(xrpCurrencyAmount.value()));
                    },
                    issuedCurrencyAmount -> {
                        json.put("SendMax", toAmount(issuedCurrencyAmount));
                    });
        }

        var memos = new JSONArray();
        for (var m : payment.memos()) {
            var memo = toJson(m.memo());
            if (memo.length() > 0) {
                memos.put(memo);
            }
        }
        if (memos.length() > 0) {
            json.put("Memos", memos);
        }

        XrplUtils.networkId(networkInfo).ifPresent(networkID -> json.put("NetworkID", networkID));

        return json;
    }

    private static JSONObject toJson(Memo memo) {
        var json = new JSONObject();
        var m = new JSONObject();
        json.put("Memo", m);

        if (memo.memoData().isPresent()) {
            m.put("MemoData", memo.memoData().get());
        }
        if (memo.memoFormat().isPresent()) {
            m.put("MemoFormat", memo.memoFormat().get());
        }
        if (memo.memoType().isPresent()) {
            m.put("MemoType", memo.memoType().get());
        }
        return json;
    }

    private static JSONObject toAmount(IssuedCurrencyAmount amt) {
        var json = new JSONObject();
        json.put("currency", amt.currency());
        json.put("value", amt.value());
        json.put("issuer", amt.issuer().value());
        return json;
    }
}
