package com.radynamics.dallipay.cryptoledger.xrpl.paystring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Resolver {
    private final static Logger log = LogManager.getLogger(Resolver.class);

    public ResolverResult discover(PayString payString) throws PayStringException {
        JSONObject jsonResult;
        try {
            jsonResult = load(payString);
        } catch (IOException e) {
            throw new PayStringException(e.getMessage(), e);
        }

        if (jsonResult == null) {
            return null;
        }

        var result = new ResolverResult(jsonResult.getString("payId"));
        result.setAddresses(toAddresses(jsonResult.optJSONArray("addresses")));
        return result;
    }

    private Address[] toAddresses(JSONArray jsonAddresses) {
        if (jsonAddresses == null || jsonAddresses.length() == 0) {
            return new Address[0];
        }

        var r = new Address[jsonAddresses.length()];
        for (var i = 0; i < jsonAddresses.length(); i++) {
            r[i] = toAddress(jsonAddresses.getJSONObject(i));
        }
        return r;
    }

    private Address toAddress(JSONObject jsonAddress) {
        var r = new Address();
        r.setPaymentNetwork(jsonAddress.optString("paymentNetwork"));
        r.setEnvironment(jsonAddress.optString("environment"));
        if ("CryptoAddressDetails".equalsIgnoreCase(jsonAddress.optString("addressDetailsType"))) {
            r.setDetails(toDetails(jsonAddress.optJSONObject("addressDetails")));
        }
        return r;
    }

    private CryptoAddressDetails toDetails(JSONObject jsonDetails) {
        var r = new CryptoAddressDetails();
        r.setAddress(jsonDetails.optString("address"));
        r.setTag(jsonDetails.optString("tag"));
        return r;
    }

    private JSONObject load(PayString payString) throws IOException, PayStringException {
        // Eg. payString "walletall$mightypirates.sandbox.paystring.org" -> https://mightypirates.sandbox.paystring.org/walletall
        var url = new URL("https://%s/%s".formatted(payString.domain(), payString.name()));

        var conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(2000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("PayID-Version", "1.0");
        conn.setRequestProperty("Accept", "application/payid+json");
        conn.connect();

        var responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new PayStringException(String.format("Failed to get info for %s due HttpResponseCode %s", payString.getValue(), responseCode));
        }

        var br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        var sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            if (sb.length() > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append(line);
        }

        try {
            return new JSONObject(sb.toString());
        } catch (JSONException e) {
            log.trace(e.getMessage(), e);
            return null;
        }
    }
}
