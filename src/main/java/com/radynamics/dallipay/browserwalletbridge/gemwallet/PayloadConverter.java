package com.radynamics.dallipay.browserwalletbridge.gemwallet;

import com.google.common.primitives.UnsignedLong;
import com.radynamics.dallipay.cryptoledger.FeeHelper;
import com.radynamics.dallipay.cryptoledger.FeeType;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Utils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;

public class PayloadConverter implements com.radynamics.dallipay.browserwalletbridge.PayloadConverter {
    public JSONObject toJson(Transaction t) {
        if (t == null) throw new IllegalArgumentException("Parameter 't' cannot be null");

        var json = new JSONObject();
        var ccy = t.getAmount().getCcy();
        if (ccy.getCode().equals(t.getLedger().getNativeCcySymbol())) {
            json.put("amount", xrpToDrops(t.getAmount()));
        } else {
            json.put("amount", t.getAmount().getNumber());
            json.put("currency", ccy.getCode());
            json.put("issuer", ccy.getIssuer().getPublicKey());
        }
        json.put("destination", t.getReceiverWallet().getPublicKey());

        var destTagBuilder = t.getLedger().createDestinationTagBuilder();
        if (destTagBuilder.isValid(t.getDestinationTag()) && !StringUtils.isEmpty(t.getDestinationTag())) {
            json.put("destinationTag", t.getDestinationTag());
        }

        json.put("fee", xrpToDrops(FeeHelper.get(t.getFees(), FeeType.LedgerTransactionFee).orElseThrow()));

        var memoData = com.radynamics.dallipay.cryptoledger.memo.PayloadConverter.toMemo(t.getStructuredReferences(), t.getMessages());
        if (!StringUtils.isEmpty(memoData)) {
            json.put("memos", toJsonMemo(memoData));
        }

        return json;
    }

    private static UnsignedLong xrpToDrops(Money xrpAmount) {
        if (!xrpAmount.getCcy().getCode().equals("XRP")) {
            throw new IllegalArgumentException("Amount expected in XRP and not " + xrpAmount.getCcy().getCode());
        }
        return XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(xrpAmount.getNumber().doubleValue())).value();
    }

    private static JSONArray toJsonMemo(String memoData) {
        var memosArray = new JSONArray();
        var memos = new JSONObject();
        memosArray.put(memos);
        var memo = new JSONObject();
        memos.put("memo", memo);
        memo.put("memoData", Utils.stringToHex(memoData));
        memo.put("memoFormat", Utils.stringToHex("json"));
        return memosArray;
    }
}
