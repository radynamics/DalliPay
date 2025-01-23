package com.radynamics.dallipay.paymentrequest;

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class EmbeddedServer {
    private final static Logger log = LogManager.getLogger(EmbeddedServer.class);
    private static HttpServer httpServer;
    private EmbeddedHttpHandler httpHandler;

    private final ArrayList<RequestListener> listener = new ArrayList<>();
    private final String address = "127.0.0.1";
    private int port = 58909;
    private ThreadPoolExecutor threadPoolExecutor;

    public synchronized void start() throws IOException {
        if (isHttpServerRunning()) {
            return;
        }

        httpHandler = new EmbeddedHttpHandler();
        httpHandler.addRequestListenerListener(new RequestListener() {
            @Override
            public void onPaymentRequest(URI requestUri) {
                raisePaymentRequest(requestUri);
            }

            @Override
            public void onPain001Received(Pain001Request args) {
                raisePain001Received(args);
            }
        });

        httpServer = HttpServer.create(new InetSocketAddress(address, port), 0);
        httpServer.createContext("/" + EmbeddedHttpHandler.RequestPath, httpHandler);
        httpServer.createContext("/" + EmbeddedHttpHandler.PaymentPath, httpHandler);
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

    private class EmbeddedHttpHandler implements HttpHandler {
        private final ArrayList<RequestListener> listener = new ArrayList<>();

        public static final String RequestPath = "request";
        public static final String PaymentPath = "payment";
        public static final String HealthPath = "health";

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            var uri = httpExchange.getRequestURI();

            if ("GET".equals(httpExchange.getRequestMethod())) {
                if (uri.getPath().equals("/" + RequestPath + "/")) {
                    httpExchange.sendResponseHeaders(200, 0);
                    httpExchange.close();
                    raisePaymentRequest(uri);
                    return;
                }
                if (uri.getPath().equals("/" + HealthPath + "/")) {
                    httpExchange.sendResponseHeaders(200, 0);
                    httpExchange.close();
                    raisePaymentRequest(uri);
                    return;
                }
            }
            if ("POST".equals(httpExchange.getRequestMethod())) {
                if (uri.getPath().equals("/" + PaymentPath + "/")) {
                    try {
                        var json = readJson(httpExchange.getRequestBody());
                        var pain001Base64 = json.optString("iso20022pain001", null);
                        if (pain001Base64 == null) {
                            BadRequest(httpExchange);
                            return;
                        }
                        var args = new Pain001Request(new String(Base64.getDecoder().decode(pain001Base64)));
                        args.networkId(json.optString("network", null));
                        args.accountWalletPairs(readAccountMapping(json.optJSONArray("accountmapping")));
                        raisePain001Received(args);
                    } catch (Exception e) {
                        BadRequest(httpExchange);
                        return;
                    }
                    httpExchange.sendResponseHeaders(200, 0);
                    httpExchange.close();
                    raisePaymentRequest(uri);
                    return;
                }
            }

            httpExchange.sendResponseHeaders(404, 0);
            httpExchange.close();
        }

        private List<AccountWalletPair> readAccountMapping(JSONArray jsonMapping) {
            if (jsonMapping == null) return new ArrayList<>();

            var mapping = new ArrayList<AccountWalletPair>();
            for (var i = 0; i < jsonMapping.length(); i++) {
                var m = jsonMapping.getJSONObject(i);
                mapping.add(new AccountWalletPair(m.getString("accountNo"), m.getString("wallet")));
            }
            return mapping;
        }

        private void BadRequest(HttpExchange httpExchange) throws IOException {
            httpExchange.sendResponseHeaders(400, 0);
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
    }
}
