package com.radynamics.dallipay.cryptoledger.xrpl.xahau.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import com.radynamics.dallipay.cryptoledger.LedgerException;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.xrpl.api.PaymentBuilder;
import com.radynamics.dallipay.cryptoledger.xrpl.xahau.FeeInfo;
import com.radynamics.dallipay.cryptoledger.xrpl.xahau.Ledger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WebSocketApi {
    private final Ledger ledger;
    private final URI uri;

    public WebSocketApi(Ledger ledger, URI uri) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        if (uri == null) throw new IllegalArgumentException("Parameter 'uri' cannot be null");
        this.ledger = ledger;
        this.uri = uri;
    }

    public FeeInfo fee(Transaction t) throws InterruptedException, JsonProcessingException, LedgerException {
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

    private FeeInfo fee(JSONObject transaction) throws InterruptedException, JsonProcessingException {
        var json = new JsonObject();
        json.addProperty("command", "fee");
        var codec = new XrplBinaryCodec();
        json.addProperty("tx_blob", codec.encode(transaction.toString()));

        var future = new CompletableFuture<JSONObject>();
        // TODO: open websocket connection once and check multiple transactions (-> reduce open connections)
        var client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                // do nothing
            }

            @Override
            public void onMessage(String message) {
                future.complete(new JSONObject(message));
                close();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onError(Exception ex) {
                future.completeExceptionally(ex);
            }
        };

        client.connectBlocking(5, TimeUnit.SECONDS);
        client.send(json.toString());
        return toFeeInfo(future.join());
    }

    private FeeInfo toFeeInfo(JSONObject json) {
        return new FeeInfo(ledger, json.getJSONObject("result").getJSONObject("drops").getLong("base_fee"));
    }
}
