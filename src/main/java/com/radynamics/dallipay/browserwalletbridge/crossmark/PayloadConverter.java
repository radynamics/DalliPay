package com.radynamics.dallipay.browserwalletbridge.crossmark;

import com.radynamics.dallipay.cryptoledger.FeeHelper;
import com.radynamics.dallipay.cryptoledger.FeeType;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.xrpl.Ledger;
import com.radynamics.dallipay.iso20022.Utils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class PayloadConverter implements com.radynamics.dallipay.browserwalletbridge.PayloadConverter {
    public JSONObject toJson(Transaction t) {
        if (t == null) throw new IllegalArgumentException("Parameter 't' cannot be null");

        var json = new JSONObject();
        json.put("TransactionType", "Payment");
        json.put("Account", t.getSenderWallet().getPublicKey());
        var ccy = t.getAmount().getCcy();
        if (ccy.getCode().equals(t.getLedger().getNativeCcySymbol())) {
            json.put("Amount", String.valueOf(Ledger.xrpToDrops(t.getAmount())));
        } else {
            var amt = new JSONObject();
            amt.put("value", String.valueOf(t.getAmount().getNumber()));
            amt.put("currency", ccy.getCode());
            amt.put("issuer", ccy.getIssuer().getPublicKey());
            json.put("Amount", amt);
        }
        json.put("SourceTag", com.radynamics.dallipay.cryptoledger.xrpl.Ledger.APP_ID_TAG);
        json.put("Destination", t.getReceiverWallet().getPublicKey());

        var destTagBuilder = t.getLedger().createDestinationTagBuilder();
        if (destTagBuilder.isValid(t.getDestinationTag()) && !StringUtils.isEmpty(t.getDestinationTag())) {
            json.put("DestinationTag", t.getDestinationTag());
        }

        json.put("Fee", String.valueOf(Ledger.xrpToDrops(FeeHelper.get(t.getFees(), FeeType.LedgerTransactionFee).orElseThrow())));

        var memoData = com.radynamics.dallipay.cryptoledger.memo.PayloadConverter.toMemo(t.getStructuredReferences(), t.getMessages());
        if (!StringUtils.isEmpty(memoData)) {
            json.put("Memos", toJsonMemo(memoData));
        }

        return json;
    }

    private static JSONArray toJsonMemo(String memoData) {
        var memosArray = new JSONArray();
        var memos = new JSONObject();
        memosArray.put(memos);
        var memo = new JSONObject();
        memos.put("Memo", memo);
        memo.put("MemoData", Utils.stringToHex(memoData));
        memo.put("MemoFormat", Utils.stringToHex("json"));
        return memosArray;
    }
}
