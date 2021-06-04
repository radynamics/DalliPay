package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerFactory;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeFactory;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonReader {
    public TransformInstruction read(InputStream input) {
        var bufferedReader = new BufferedReader(new InputStreamReader(input));
        var tokener = new JSONTokener(bufferedReader);
        var json = new JSONObject(tokener);

        // TODO: validate format
        var ledger = LedgerFactory.create(json.getString("ledger"));
        var ti = new TransformInstruction(ledger);
        ti.setExchange(ExchangeFactory.create(json.getString("exchange")));
        // If set to another currency than ledger's native currency, amounts are converted using rates provided by exchange.
        ti.setTargetCcy(json.getString("targetCcy"));

        var arr = json.getJSONArray("accountMapping");
        for (int i = 0; i < arr.length(); i++) {
            var obj = arr.getJSONObject(i);
            ti.add(new AccountMapping(obj.getString("iban"), obj.getString("ledgerWallet")));
        }

        return ti;
    }
}
