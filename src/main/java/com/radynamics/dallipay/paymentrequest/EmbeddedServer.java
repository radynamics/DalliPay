package com.radynamics.dallipay.paymentrequest;

import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.db.Database;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class EmbeddedServer {
    private final static Logger log = LogManager.getLogger(EmbeddedServer.class);
    private final String version;
    private static HttpServer httpServer;
    private EmbeddedHttpHandler httpHandler;

    private final ArrayList<RequestListener> listener = new ArrayList<>();
    private final String address = "127.0.0.1";
    private int port = 58909;
    private ThreadPoolExecutor threadPoolExecutor;

    public EmbeddedServer(String version) {
        this.version = version;
    }

    public synchronized void start() throws IOException {
        if (isHttpServerRunning()) {
            return;
        }

        httpHandler = new EmbeddedHttpHandler(version);
        httpHandler.addRequestListenerListener(new RequestListener() {
            @Override
            public void onPaymentRequest(URI requestUri) {
                raisePaymentRequest(requestUri);
            }

            @Override
            public void onPain001Received(Pain001Request args) {
                raisePain001Received(args);
            }

            @Override
            public void onRequestReceived(ReceiveRequest args) {
                raiseRequestReceived(args);
            }
        });

        httpServer = HttpServer.create(new InetSocketAddress(address, port), 0);
        httpServer.createContext("/" + EmbeddedHttpHandler.RequestPath, httpHandler);
        httpServer.createContext("/" + EmbeddedHttpHandler.AuthPath, httpHandler);
        httpServer.createContext("/" + EmbeddedHttpHandler.PaymentPath, httpHandler);
        httpServer.createContext("/" + EmbeddedHttpHandler.ReceivePath, httpHandler);
        httpServer.createContext("/" + EmbeddedHttpHandler.HealthPath, httpHandler);
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        httpServer.setExecutor(threadPoolExecutor);
        httpServer.start();
    }

    private boolean isHttpServerRunning() {
        return httpServer != null;
    }

    public void addRequestListenerListener(RequestListener l) {
        listener.add(l);
    }

    private void raisePaymentRequest(URI requestUri) {
        for (var l : listener) {
            l.onPaymentRequest(requestUri);
        }
    }

    private void raisePain001Received(Pain001Request args) {
        for (var l : listener) {
            l.onPain001Received(args);
        }
    }

    private void raiseRequestReceived(ReceiveRequest args) {
        for (var l : listener) {
            l.onRequestReceived(args);
        }
    }

    private class EmbeddedHttpHandler implements HttpHandler {
        private final String version;
        private final Map<String, String> sessionIds = new HashMap<>();
        private final ArrayList<RequestListener> listener = new ArrayList<>();

        public static final String RequestPath = "request";
        public static final String AuthPath = "auth";
        public static final String PaymentPath = "payment";
        public static final String ReceivePath = "receive";
        public static final String HealthPath = "health";

        public EmbeddedHttpHandler(String version) {
            this.version = version;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            var uri = httpExchange.getRequestURI();

            if ("GET".equals(httpExchange.getRequestMethod())) {
                if (isPath(uri, RequestPath)) {
                    Ok(httpExchange);
                    raisePaymentRequest(uri);
                    return;
                }
                if (isPath(uri, HealthPath)) {
                    Ok(httpExchange);
                    raisePaymentRequest(uri);
                    return;
                }
            }
            if ("POST".equals(httpExchange.getRequestMethod())) {
                if (isPath(uri, AuthPath)) {
                    handleAuth(httpExchange);
                    return;
                }
                if (isPath(uri, PaymentPath)) {
                    handlePayment(httpExchange);
                    return;
                }
                if (isPath(uri, ReceivePath)) {
                    handleReceive(httpExchange);
                    return;
                }
            }

            httpExchange.sendResponseHeaders(404, 0);
            httpExchange.close();
        }

        private void handleAuth(HttpExchange httpExchange) throws IOException {
            var json = readJson(httpExchange.getRequestBody());
            if (!Database.isReadable(json.optString("password", null))) {
                Forbidden(httpExchange);
                return;
            }

            var sessionId = createSessionId(json.optString("applicationName", "unknown"));
            var payload = new JSONObject();
            payload.put("sessionid", sessionId);
            payload.put("version", version);
            Ok(httpExchange, createSuccessJson(payload));
        }

        private void handlePayment(HttpExchange httpExchange) throws IOException {
            try {
                if (!assertValidSessionId(httpExchange)) {
                    return;
                }
                var json = readJson(httpExchange.getRequestBody());
                var pain001Base64 = json.optString("iso20022pain001", null);
                if (pain001Base64 == null) {
                    BadRequest(httpExchange);
                    return;
                }
                var args = new Pain001Request(new String(Base64.getDecoder().decode(pain001Base64)));
                args.applicationName(getApplicationName(httpExchange).orElse("unknown"));
                args.ledgerId(LedgerId.ofExternalId(json.optString("network", null)).orElse(null));
                args.accountWalletPairs(readAccountMapping(json.optJSONArray("accountmapping")));
                raisePain001Received(args);

                waitUntilTimeout(httpExchange, () -> {
                    if (args.aborted()) {
                        Ok(httpExchange, createErrorJson("user_aborted"));
                        return true;
                    }
                    if (args.sent()) {
                        var payload = new JSONObject();
                        payload.put("remainingxml", args.remainingPain001() == null ? null : Base64.getEncoder().encodeToString(args.remainingPain001().toByteArray()));
                        payload.put("countsent", args.countSent());
                        payload.put("counttotal", args.countTotal());
                        Ok(httpExchange, createSuccessJson(payload));
                        raisePaymentRequest(httpExchange.getRequestURI());
                        return true;
                    }
                    return false;
                });
            } catch (Exception e) {
                BadRequest(httpExchange);
            }
        }

        private void handleReceive(HttpExchange httpExchange) throws IOException {
            try {
                if (!assertValidSessionId(httpExchange)) {
                    return;
                }
                var json = readJson(httpExchange.getRequestBody());
                var df = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss");
                var now = LocalDateTime.now();
                var from = json.optString("from", df.format(now.minusMonths(1)));
                var to = json.optString("to", df.format(now));

                var args = new ReceiveRequest();
                args.applicationName(getApplicationName(httpExchange).orElse("unknown"));
                args.wallet(json.optString("wallet", null));
                args.format(json.optString("format", "camt053"));
                args.from(LocalDateTime.parse(from, df));
                args.to(LocalDateTime.parse(to, df));
                args.ledgerId(LedgerId.ofExternalId(json.optString("network", null)).orElse(null));
                args.accountWalletPairs(readAccountMapping(json.optJSONArray("accountmapping")));
                raiseRequestReceived(args);

                waitUntilTimeout(httpExchange, () -> {
                    if (args.aborted()) {
                        var payload = new JSONObject();
                        payload.put("xml", (String) null);
                        Ok(httpExchange, createSuccessJson(payload));
                        return true;
                    }
                    if (args.camtXml() != null) {
                        var payload = new JSONObject();
                        payload.put("xml", Base64.getEncoder().encodeToString(args.camtXml().getBytes()));
                        Ok(httpExchange, createSuccessJson(payload));
                        return true;
                    }
                    return false;
                });
            } catch (Exception e) {
                BadRequest(httpExchange);
            }
        }

        private void waitUntilTimeout(HttpExchange httpExchange, Callable<Boolean> action) {
            Executors.newSingleThreadExecutor().execute(() -> {
                var remainingTime = Duration.ofMinutes(5);
                while (remainingTime.toMillis() > 0) {
                    try {
                        if (action.call()) {
                            return;
                        }
                    } catch (Exception e) {
                        InternalError(httpExchange);
                        return;
                    }

                    var wait = Duration.ofMillis(500);
                    try {
                        Thread.sleep(wait.toMillis());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    remainingTime = remainingTime.minus(wait);
                }
                try {
                    Ok(httpExchange, createErrorJson("timeout"));
                } catch (IOException e) {
                    InternalError(httpExchange);
                }
            });
        }

        private boolean assertValidSessionId(HttpExchange httpExchange) throws IOException {
            var sessionId = getSessionIdOrNull(httpExchange);
            if (sessionId == null) {
                BadRequest(httpExchange);
                return false;
            }
            if (!sessionIds.keySet().contains(sessionId)) {
                Forbidden(httpExchange);
                return false;
            }
            return true;
        }

        private String getSessionIdOrNull(HttpExchange httpExchange) {
            var list = httpExchange.getRequestHeaders().getOrDefault("sessionid", null);
            if (list == null) {
                return null;
            }
            return list.stream().findFirst().orElse(null);
        }

        private Optional<String> getApplicationName(HttpExchange httpExchange) {
            var sessionId = getSessionIdOrNull(httpExchange);
            if (sessionId == null) {
                return Optional.empty();
            }
            return Optional.of(sessionIds.get(sessionId));
        }

        private String createSessionId(String applicationName) {
            var sessionId = UUID.randomUUID().toString().replace("-", "");
            sessionIds.put(sessionId, applicationName);
            return sessionId;
        }

        private JSONObject createSuccessJson(JSONObject payload) {
            var result = new JSONObject();
            result.put("success", true);
            result.put("data", payload);
            return result;
        }

        private JSONObject createErrorJson(String errorKey) {
            var result = new JSONObject();
            result.put("success", false);
            var error = new JSONObject();
            error.put("key", errorKey);
            result.put("error", error);
            return result;
        }

        private boolean isPath(URI uri, String path) {
            return uri.getPath().equals("/" + path) || uri.getPath().equals("/" + path + "/");
        }

        private List<AccountWalletPair> readAccountMapping(JSONArray jsonMapping) {
            if (jsonMapping == null) return new ArrayList<>();

            var mapping = new ArrayList<AccountWalletPair>();
            for (var i = 0; i < jsonMapping.length(); i++) {
                var m = jsonMapping.getJSONObject(i);
                mapping.add(new AccountWalletPair(m.getString("accountno"), m.getString("wallet")));
            }
            return mapping;
        }

        private void Ok(HttpExchange httpExchange) throws IOException {
            Return(httpExchange, 200);
        }

        private void Ok(HttpExchange httpExchange, JSONObject response) throws IOException {
            byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
            httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=" + StandardCharsets.UTF_8);
            httpExchange.sendResponseHeaders(200, bytes.length);

            var outputStream = httpExchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        }

        private void BadRequest(HttpExchange httpExchange) throws IOException {
            Return(httpExchange, 400);
        }

        private void Forbidden(HttpExchange httpExchange) throws IOException {
            Return(httpExchange, 403);
        }

        private void InternalError(HttpExchange httpExchange) {
            try {
                Return(httpExchange, 500);
            } catch (IOException e) {
                // Occurs if peer closed the connection.
                log.info(e.getMessage(), e);
            }
        }

        private void Return(HttpExchange httpExchange, int httpCode) throws IOException {
            httpExchange.sendResponseHeaders(httpCode, 0);
            httpExchange.close();
        }

        private JSONObject readJson(InputStream in) throws IOException {
            var sr = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            var sb = new StringBuilder();

            String inputStr;
            while ((inputStr = sr.readLine()) != null)
                sb.append(inputStr);

            return new JSONObject(sb.toString());
        }

        public void addRequestListenerListener(RequestListener l) {
            listener.add(l);
        }

        private void raisePaymentRequest(URI requestUri) {
            for (var l : listener) {
                l.onPaymentRequest(requestUri);
            }
        }

        private void raisePain001Received(Pain001Request args) {
            for (var l : listener) {
                l.onPain001Received(args);
            }
        }

        private void raiseRequestReceived(ReceiveRequest args) {
            for (var l : listener) {
                l.onRequestReceived(args);
            }
        }
    }
}
