package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.UUID;

public class XummApi {
    private final static Logger log = LogManager.getLogger(XummApi.class);

    private String accessToken;
    private final ArrayList<XummApiListener> listener = new ArrayList<>();

    private static final String baseUri = "https://xumm.app/api/v1/jwt";

    public XummApi() {
        this(null);
    }

    public XummApi(String accessToken) {
        this.accessToken = accessToken;
    }

    public JSONObject submit(JSONObject payload) throws IOException, InterruptedException, XummException {
        var json = new JSONObject();
        json.put("txjson", payload);

        return post("/payload", HttpRequest.BodyPublishers.ofString(json.toString()));
    }

    public JSONObject status(UUID payloadId) throws IOException, InterruptedException, XummException {
        return get("/payload/" + payloadId);
    }

    private JSONObject get(String path) throws IOException, InterruptedException, XummException {
        return send(createRequestBuilder(path).GET().build());
    }

    private JSONObject post(String path, HttpRequest.BodyPublisher body) throws IOException, InterruptedException, XummException {
        return send(createRequestBuilder(path).POST(body).build());
    }

    private JSONObject send(HttpRequest request) throws IOException, InterruptedException, XummException {
        if (accessToken == null) throw new IllegalArgumentException("Parameter 'accessToken' cannot be null");

        var client = HttpClient.newHttpClient();
        var httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        logRateLimit(httpResponse.headers());
        var responseText = httpResponse.body();
        JSONObject response;
        try {
            response = new JSONObject(responseText);
        } catch (Exception e) {
            throw new XummException(e.getMessage(), e);
        }

        var error = response.optJSONObject("error");
        if (error != null) {
            throw new XummException(String.format("Xumm API error: %s, reference %s", error.getInt("code"), error.getString("reference")));
        }

        var errorText = response.optString("error", null);
        if (errorText != null) {
            if (errorText.contains("JWT expired")) {
                log.info(errorText);
                // ex "{"error":"JWT expired, request a new one using the `/authorize` endpoint"}"
                raiseAccessTokenExpired();
                return null;
            }
            throw new XummException(String.format("Invalid JSON Xumm API response: %s", responseText));
        }

        return response;
    }

    private void logRateLimit(HttpHeaders headers) {
        final String nameRemaining = "X-RateLimit-Remaining";
        var remainingText = headers.firstValue(nameRemaining).orElse(null);
        if (remainingText == null) {
            log.warn(String.format("Header %s was not present in response.", nameRemaining));
            return;
        }

        var limitText = headers.firstValue("X-RateLimit-Limit").orElse("0");
        var msg = String.format("Remaining %s/%s calls within rateLimit.", remainingText, limitText);
        if (Integer.parseInt(remainingText) >= 10) {
            log.info(msg);
            return;
        }
        log.warn(msg);
    }

    private HttpRequest.Builder createRequestBuilder(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUri + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken);
    }

    public void addListener(XummApiListener l) {
        listener.add(l);
    }

    private void raiseAccessTokenExpired() {
        for (var l : listener) {
            l.onAccessTokenExpired();
        }
    }

    public void setAccessToken(String accessToken) {
        if (accessToken == null) throw new IllegalArgumentException("Parameter 'accessToken' cannot be null");
        this.accessToken = accessToken;
    }
}
