package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class XrplPriceOracleConfig {
    private final HashSet<IssuedCurrency> issuedCurrencies = new HashSet<>();

    public void load() {
        var tmp = new HashSet<IssuedCurrency>();
        tmp.add(new IssuedCurrency(new CurrencyPair("XRP", "USD"), new Wallet("r9PfV3sQpKLWxccdg3HL2FXKxGW2orAcLE"), new Wallet("rXUMMaPpZqPutoRszR29jtC8amWq3APkx")));
        tmp.add(new IssuedCurrency(new CurrencyPair("XRP", "JPY"), new Wallet("r9PfV3sQpKLWxccdg3HL2FXKxGW2orAcLE"), new Wallet("rrJPYwVRyWFcwfaNMm83QEaCexEpKnkEg")));

        var instances = fromJson(toJson(tmp));
        issuedCurrencies.addAll(Arrays.asList(instances));
    }

    private JSONObject toJson(Collection<IssuedCurrency> ccys) {
        var o = new JSONObject();

        o.put("version", 1);
        var ccyPairs = new JSONArray();
        o.put("ccyPairs", ccyPairs);
        for (var ccy : ccys) {
            var k = new JSONObject();
            ccyPairs.put(k);
            k.put("first", ccy.getPair().getFirst());
            k.put("second", ccy.getPair().getSecond());
            k.put("issuer", ccy.getIssuer().getPublicKey());
            k.put("receiver", ccy.getIssuer().getPublicKey());
        }

        return o;
    }

    private IssuedCurrency[] fromJson(JSONObject json) {
        var list = new ArrayList<IssuedCurrency>();

        var ccyPairs = json.getJSONArray("ccyPairs");
        for (var i = 0; i < ccyPairs.length(); i++) {
            var k = ccyPairs.getJSONObject(i);
            var pair = new CurrencyPair(k.getString("first"), k.getString("second"));
            list.add(new IssuedCurrency(pair, new Wallet(k.getString("issuer")), new Wallet((k.getString("receiver")))));
        }

        return list.toArray(new IssuedCurrency[0]);
    }

    public IssuedCurrency[] issuedCurrencies() {
        return issuedCurrencies.toArray(new IssuedCurrency[0]);
    }
}
