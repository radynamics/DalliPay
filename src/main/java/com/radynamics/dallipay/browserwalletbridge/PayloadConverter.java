package com.radynamics.dallipay.browserwalletbridge;

import com.radynamics.dallipay.cryptoledger.Transaction;
import org.json.JSONObject;

public interface PayloadConverter {
    JSONObject toJson(Transaction t);
}
