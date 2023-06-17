package com.radynamics.dallipay.browserwalletbridge.metamask;

import com.radynamics.dallipay.cryptoledger.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PayloadConverter implements com.radynamics.dallipay.browserwalletbridge.PayloadConverter {
    public JSONObject toJson(Transaction t) {
        if (t == null) throw new IllegalArgumentException("Parameter 't' cannot be null");

        var json = new JSONObject();
        var ccy = t.getAmount().getCcy();
        var isEth = ccy.equals("ETH");
        if (!isEth) {
            json.put("issuer", ccy.getIssuer().getPublicKey());
        }
        json.put("amount", toWei(t.getAmount().getNumber().doubleValue()));
        json.put("destination", t.getReceiverWallet().getPublicKey());

        var memoData = com.radynamics.dallipay.cryptoledger.memo.PayloadConverter.toMemo(t.getStructuredReferences(), t.getMessages());
        if (!StringUtils.isEmpty(memoData)) {
            if (!isEth) {
                throw new IllegalArgumentException("Memos are only available for transactions in ETH. ERC20 payments don't support payloads.");
            }
            json.put("memos", memoData);
        }

        return json;
    }

    private BigInteger toWei(Double amount) {
        var factor = BigDecimal.TEN.pow(18);
        return BigDecimal.valueOf(amount).multiply(factor).toBigInteger();
    }
}
