package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.beans.ExceptionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class OAuth2PkceAuthentication implements OAuth2PkceListener {
    private final String basePath;
    private final String clientId;
    private String scope;
    private static HttpServer httpServer;
    private static PkceHttpHandler httpHandler;
    private static String codeVerifier;

    private final ArrayList<OAuth2PkceAuthenticationListener> authenticationListener = new ArrayList<>();
    private final ArrayList<ExceptionListener> exceptionListener = new ArrayList<>();
    private final String address = "127.0.0.1";
    public static final int defaultPort = 58890;
    private int port = defaultPort;
    private ThreadPoolExecutor threadPoolExecutor;

    public OAuth2PkceAuthentication(String basePath, String clientId) {
        if (basePath == null) throw new IllegalArgumentException("Parameter 'basePath' cannot be null");
        if (clientId == null) throw new IllegalArgumentException("Parameter 'clientId' cannot be null");
        this.basePath = basePath;
        this.clientId = clientId;
    }

    public void authenticate() {
        try {
            startHttpServer();

            var sb = new StringBuilder();
            sb.append(basePath + "/auth");
            sb.append("?response_type=code");
            sb.append("&client_id=" + clientId);
            if (getScope() != null) {
                sb.append("&scope=" + URLEncoder.encode(getScope(), StandardCharsets.UTF_8));
            }
            sb.append("&redirect_uri=" + createRedirectUri(httpHandler.AuthPath));
            codeVerifier = createCodeVerifier();
            sb.append("&code_challenge=" + createCodeChallenge(codeVerifier));
            sb.append("&code_challenge_method=S256");

            raiseOpenInBrowser(URI.create(sb.toString()));
        } catch (IOException | NoSuchAlgorithmException e) {
            raiseException(e);
        }
    }

    @Override
    public void onAuthorizationCodeReceived(String code) {
        try {
            var responseToken = exchangeForAccessToken(code);
            if (!responseToken.isNull("error")) {
                raiseException(throwException(responseToken));
                return;
            }
            raiseAuthorized(responseToken.getString("access_token"));
        } catch (IOException | InterruptedException e) {
            raiseException(new OAuth2Exception("Could not read access_token", e));
        } catch (OAuth2Exception e) {
            raiseException(e);
        } finally {
            stopHttpServer();
        }
    }

    protected OAuth2Exception throwException(JSONObject errorResponse) throws OAuth2Exception {
        throw new OAuth2Exception(String.format("%s: %s", errorResponse.getString("error"), errorResponse.getString("error_description")));
    }

    private JSONObject exchangeForAccessToken(String code) throws IOException, InterruptedException {
        var sb = new StringBuilder();
        sb.append("grant_type=authorization_code");
        sb.append("&code=" + code);
        sb.append("&client_id=" + clientId);
        sb.append("&redirect_uri=" + createRedirectUri(httpHandler.AuthPath));
        sb.append("&code_verifier=" + codeVerifier);
        var requestBody = sb.toString();

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(basePath + "/token"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return new JSONObject(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
    }

    private static String createCodeVerifier() {
        var sr = new SecureRandom();
        var code = new byte[32];
        sr.nextBytes(code);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }

    private static String createCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        var bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        var md = MessageDigest.getInstance("SHA-256");
        md.update(bytes, 0, bytes.length);
        var digest = md.digest();
        return org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(digest);
    }

    private String createRedirectUri(String path) {
        return String.format("%s/%s", getBaseUri(), path);
    }

    private String getBaseUri() {
        return "http://" + address + ":" + port;
    }

    private synchronized void startHttpServer() throws IOException {
        if (isHttpServerRunning()) {
            return;
        }

        codeVerifier = null;
        httpHandler = new PkceHttpHandler();
        httpHandler.addOAuth2PkceListener(this);

        httpServer = HttpServer.create(new InetSocketAddress(address, port), 0);
        httpServer.createContext("/" + httpHandler.AuthPath, httpHandler);
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        httpServer.setExecutor(threadPoolExecutor);
        httpServer.start();
    }

    private synchronized void stopHttpServer() {
        if (!isHttpServerRunning()) {
            return;
        }

        codeVerifier = null;
        httpHandler = null;

        httpServer.stop(0);
        threadPoolExecutor.shutdown();
        httpServer = null;
    }

    private boolean isHttpServerRunning() {
        return httpServer != null;
    }

    public void addOAuth2PkceAuthenticationListener(OAuth2PkceAuthenticationListener l) {
        authenticationListener.add(l);
    }

    private void raiseOpenInBrowser(URI uri) {
        for (var l : authenticationListener) {
            l.onOpenInBrowser(uri);
        }
    }

    private void raiseAuthorized(String accessToken) {
        for (var l : authenticationListener) {
            l.onAuthorized(accessToken);
        }
    }

    public void addExceptionListener(ExceptionListener l) {
        exceptionListener.add(l);
    }

    private void raiseException(Exception e) {
        for (var l : exceptionListener) {
            l.exceptionThrown(e);
        }
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private class PkceHttpHandler implements HttpHandler {
        private final ArrayList<OAuth2PkceListener> listener = new ArrayList<>();

        private final String AuthPath = "auth";

        private final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            var uri = httpExchange.getRequestURI();

            if ("GET".equals(httpExchange.getRequestMethod())) {
                var requestParams = splitQuery(httpExchange.getRequestURI().getQuery());
                if (uri.getPath().equals("/" + AuthPath)) {
                    if (requestParams.containsKey("error")) {
                        responseError(httpExchange, requestParams.get("error_description").get(0));
                        return;
                    }
                    var code = requestParams.get("code").get(0);
                    responseOk(httpExchange);
                    raiseAuthorizationCodeReceived(code);
                }
            }

            httpExchange.sendResponseHeaders(404, 0);
        }

        private void responseOk(HttpExchange httpExchange) throws IOException {
            response(httpExchange, createText(res.getString("pkceHttpHandler.ok.title"), res.getString("pkceHttpHandler.ok.desc")));
        }

        private void responseError(HttpExchange httpExchange, String errorDescription) throws IOException {
            response(httpExchange, createText(res.getString("pkceHttpHandler.error.title"), String.format(res.getString("pkceHttpHandler.error.desc"), errorDescription)));
        }

        private String createText(String title, String desc) {
            var sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html><body><h2>");
            sb.append(title);
            sb.append("</h2><p>");
            sb.append(desc);
            sb.append("</p></body></html>");
            return sb.toString();
        }

        private void response(HttpExchange httpExchange, String responseText) throws IOException {
            byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
            httpExchange.sendResponseHeaders(200, bytes.length);

            var outputStream = httpExchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        }

        public Map<String, List<String>> splitQuery(String queryString) {
            if (queryString == null || queryString.length() == 0) {
                return Collections.emptyMap();
            }
            return Arrays.stream(queryString.split("&"))
                    .map(this::splitQueryParameter)
                    .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        }

        public AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
            var idx = it.indexOf("=");
            var key = idx > 0 ? it.substring(0, idx) : it;
            var value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
            return new AbstractMap.SimpleImmutableEntry<>(
                    URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8)
            );
        }

        public void addOAuth2PkceListener(OAuth2PkceListener l) {
            listener.add(l);
        }

        private void raiseAuthorizationCodeReceived(String code) {
            for (var l : listener) {
                l.onAuthorizationCodeReceived(code);
            }
        }
    }
}
