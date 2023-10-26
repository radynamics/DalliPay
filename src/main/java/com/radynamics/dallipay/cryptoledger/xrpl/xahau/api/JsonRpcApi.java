package com.radynamics.dallipay.cryptoledger.xrpl.xahau.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.radynamics.dallipay.cryptoledger.LedgerException;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.xrpl.api.PaymentBuilder;
import com.radynamics.dallipay.cryptoledger.xrpl.xahau.FeeInfo;
import com.radynamics.dallipay.cryptoledger.xrpl.xahau.Ledger;
import okhttp3.HttpUrl;
import org.json.JSONObject;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class JsonRpcApi {
    private final Ledger ledger;
    private final HttpUrl url;

    public JsonRpcApi(Ledger ledger, HttpUrl url) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        if (url == null) throw new IllegalArgumentException("Parameter 'url' cannot be null");
        this.ledger = ledger;
        this.url = url;
    }

    public FeeInfo fee(Transaction t) throws InterruptedException, IOException, LedgerException {
        var pb = PaymentBuilder.builder().payment(t);
        var builder = pb.build();
        // Fee must be 0, SigningPubKey empty (https://xrpl-hooks.readme.io/docs/hook-fees)
        builder.fee(XrpCurrencyAmount.ofDrops(0));
        builder.signingPublicKey("");
        var payment = builder.build();

        var objectMapper = ObjectMapperFactory.create();
        var json = new JSONObject(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payment));
        return fee(json);
    }

    private FeeInfo fee(JSONObject transaction) throws InterruptedException, IOException {
        var json = new JsonObject();
        json.addProperty("method", "fee");
        var params = new JsonArray();
        json.add("params", params);
        var txBlob = new JsonObject();
        params.add(txBlob);
        var codec = new XrplBinaryCodec();
        txBlob.addProperty("tx_blob", codec.encode(transaction.toString()));

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(url.uri())
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        return toFeeInfo(new JSONObject(client.send(request, HttpResponse.BodyHandlers.ofString()).body()));
    }

    private FeeInfo toFeeInfo(JSONObject json) {
        return new FeeInfo(ledger, json.getJSONObject("result").getJSONObject("drops").getLong("base_fee"));
    }
}
