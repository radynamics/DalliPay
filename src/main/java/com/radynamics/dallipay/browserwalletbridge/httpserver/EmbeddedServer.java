package com.radynamics.dallipay.browserwalletbridge.httpserver;

import com.radynamics.dallipay.browserwalletbridge.BridgeException;
import com.radynamics.dallipay.browserwalletbridge.BrowserApi;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class EmbeddedServer {
    private static HttpServer httpServer;
    private EmbeddedHttpHandler httpHandler;

    private final ArrayList<BridgeEventListener> listener = new ArrayList<>();
    private final String address = "127.0.0.1";
    private int port = 58905;
    private final BrowserApi browserApi;
    private ThreadPoolExecutor threadPoolExecutor;
    private Transaction transaction;
    private CompletableFuture<Transaction> future;

    public EmbeddedServer(BrowserApi browserApi) {
        if (browserApi == null) throw new IllegalArgumentException("Parameter 'browserApi' cannot be null");
        this.browserApi = browserApi;
    }

    public CompletableFuture sendPayment(Transaction transaction, JSONObject payload) throws IOException, BridgeException {
        this.transaction = transaction;
        this.future = new CompletableFuture<>();

        var tx = Base64.encodeBase64URLSafeString(payload.toString().getBytes(StandardCharsets.UTF_8));
        String baseUri = "http://" + address + ":" + port;
        var uri = URI.create(String.format("%s/%s?amt=%s&ccy=%s&tx=%s", baseUri, EmbeddedHttpHandler.SendPath, transaction.getAmount(), transaction.getCcy(), tx));
        openInBrowser(uri);

        return this.future;
    }

    public void openInBrowser(URI uri) throws IOException, BridgeException {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new BridgeException("No desktop or no browsing supported");
        }

        Desktop.getDesktop().browse(uri);
    }

    public synchronized void start() throws IOException {
        if (isHttpServerRunning()) {
            return;
        }

        httpHandler = new EmbeddedHttpHandler();
        httpHandler.addBridgeEventListener(new BridgeEventListener() {
            @Override
            public void onPayloadSent(Transaction t, String txHash) {
                future.complete(t);
                raisePayloadSent(t, txHash);
            }

            @Override
            public void onError(Transaction t, String key, String message) {
                future.complete(t);
                raiseError(t, key, message);
            }
        });

        httpServer = HttpServer.create(new InetSocketAddress(address, port), 0);
        httpServer.createContext("/" + EmbeddedHttpHandler.SendPath, httpHandler);
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        httpServer.setExecutor(threadPoolExecutor);
        httpServer.start();
    }

    public synchronized void stopHttpServer() {
        if (!isHttpServerRunning()) {
            return;
        }

        httpHandler = null;

        httpServer.stop(0);
        threadPoolExecutor.shutdown();
        httpServer = null;
    }

    private boolean isHttpServerRunning() {
        return httpServer != null;
    }

    public void addBridgeEventListener(BridgeEventListener l) {
        listener.add(l);
    }

    private void raisePayloadSent(Transaction t, String txHash) {
        for (var l : listener) {
            l.onPayloadSent(t, txHash);
        }
    }

    private void raiseError(Transaction t, String key, String message) {
        for (var l : listener) {
            l.onError(t, key, message);
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private class EmbeddedHttpHandler implements HttpHandler {
        private final ArrayList<BridgeEventListener> listener = new ArrayList<>();

        public static final String SendPath = "send";
        private static final String CommonPath = "common";

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            var uri = httpExchange.getRequestURI();

            if ("GET".equals(httpExchange.getRequestMethod())) {
                if (uri.getPath().equals("/" + SendPath)) {
                    response(httpExchange, browserApi.createSendRequestResponse());
                    return;
                }

                var content = getContent(uri.getPath().substring(SendPath.length() + 1));
                if (content == null) {
                    httpExchange.sendResponseHeaders(404, 0);
                } else {
                    var ext = uri.getPath().substring(uri.getPath().lastIndexOf(".") + 1);
                    response(httpExchange, content, extToMimeType(ext).orElse("text/html"));
                }
                return;
            }
            if ("POST".equals(httpExchange.getRequestMethod())) {
                if (uri.getPath().equals("/" + SendPath)) {
                    var br = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8));
                    var json = new JSONObject(br.readLine());

                    var error = json.optJSONObject("error");
                    if (error != null) {
                        raiseError(transaction, error.getString("key"), error.getString("message"));
                    } else {
                        raisePayloadSent(transaction, json.getJSONObject("data").getString("txHash"));
                    }

                    response(httpExchange, "");
                }
                return;
            }

            httpExchange.sendResponseHeaders(404, 0);
        }

        private Optional<String> extToMimeType(String ext) {
            if (ext == null) {
                return Optional.empty();
            }
            switch (ext.toLowerCase(Locale.ROOT)) {
                case "js":
                    return Optional.of("text/javascript");
                case "css":
                    return Optional.of("text/css");
                default:
                    return Optional.empty();
            }
        }

        private String getContent(String path) {
            var is = getClass().getClassLoader().getResourceAsStream("browserwalletbridge" + path);
            try {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.out.println(e);
                return null;
            }
        }

        private void response(HttpExchange httpExchange, String responseText) throws IOException {
            response(httpExchange, responseText, "text/html");
        }

        private void response(HttpExchange httpExchange, String responseText, String mimeType) throws IOException {
            byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
            httpExchange.getResponseHeaders().set("Content-Type", mimeType + "; charset=" + StandardCharsets.UTF_8);
            httpExchange.sendResponseHeaders(200, bytes.length);

            var outputStream = httpExchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        }

        public void addBridgeEventListener(BridgeEventListener l) {
            listener.add(l);
        }

        private void raisePayloadSent(Transaction t, String txHash) {
            for (var l : listener) {
                l.onPayloadSent(t, txHash);
            }
        }

        private void raiseError(Transaction t, String key, String message) {
            for (var l : listener) {
                l.onError(t, key, message);
            }
        }
    }
}
