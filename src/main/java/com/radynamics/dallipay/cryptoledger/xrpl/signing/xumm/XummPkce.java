package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class XummPkce extends OAuth2PkceAuthentication {
    private String accessToken;

    private static final String basePath = "https://oauth2.xumm.app";

    public XummPkce(String apiKey) {
        this(apiKey, null);
    }

    public XummPkce(String apiKey, String accessToken) {
        super(basePath, apiKey);
        this.accessToken = accessToken;
        setScope("JavaPkce");
    }

    public static CompletableFuture<String> authenticateAsync(String apiKey, String scope, int port) {
        var authentication = new CompletableFuture<String>();
        Executors.newCachedThreadPool().submit(() -> {
            var pkce = new XummPkce(apiKey);
            pkce.setScope(scope);
            pkce.setPort(port);
            pkce.addExceptionListener(authentication::completeExceptionally);
            pkce.addOAuth2PkceAuthenticationListener(new OAuth2PkceAuthenticationListener() {
                @Override
                public void onAuthorized(String accessToken) {
                    pkce.accessToken = accessToken;
                    authentication.complete(accessToken);
                }

                @Override
                public void onOpenInBrowser(URI uri) {
                    if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        authentication.completeExceptionally(new OAuth2Exception("No desktop or no browsing supported"));
                        return;
                    }

                    try {
                        Desktop.getDesktop().browse(uri);
                    } catch (Exception e) {
                        authentication.completeExceptionally(e);
                    }
                }
            });
            pkce.authenticate();
        });
        return authentication;
    }

    public JSONObject userInfo() throws IOException, InterruptedException, OAuth2Exception {
        return post("userinfo");
    }

    private JSONObject post(String path) throws IOException, InterruptedException, OAuth2Exception {
        return post(path, null);
    }

    private JSONObject post(String path, String body) throws IOException, InterruptedException, OAuth2Exception {
        ensureAuthorized();

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(basePath + "/" + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .header("Authorization", "Bearer " + accessToken)
                .POST(body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = new JSONObject(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
        if (!response.isNull("error")) {
            throwException(response);
        }
        return response;
    }

    private void ensureAuthorized() throws OAuth2Exception {
        if (accessToken == null) {
            throw new OAuth2Exception("Not yet authorized, missing accessToken.");
        }
    }
}
