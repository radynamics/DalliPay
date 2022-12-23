package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.UUID;

public class XummApi {
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
        var response = new JSONObject(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
        if (!response.isNull("error")) {
            throwException(response);
        }
        return response;
    }

    private HttpRequest.Builder createRequestBuilder(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUri + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken);
    }

    private XummException throwException(JSONObject errorResponse) throws XummException {
        var error = errorResponse.optJSONObject("error");
        if (error == null) {
            // ex "{"error":"JWT expired, request a new one using the `/authorize` endpoint"}"
            raiseAccessTokenExpired();
            throw new XummException(String.format("Xumm API error: %s", errorResponse.getString("error")));
        }
        throw new XummException(String.format("Xumm API error: %s, reference %s", error.getInt("code"), error.getString("reference")));
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
