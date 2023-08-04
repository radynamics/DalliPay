package com.radynamics.dallipay.paymentrequest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
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
        });

        httpServer = HttpServer.create(new InetSocketAddress(address, port), 0);
        httpServer.createContext("/" + EmbeddedHttpHandler.RequestPath, httpHandler);
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

    private class EmbeddedHttpHandler implements HttpHandler {
        private final ArrayList<RequestListener> listener = new ArrayList<>();

        public static final String RequestPath = "request";

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
            }

            httpExchange.sendResponseHeaders(404, 0);
            httpExchange.close();
        }

        public void addRequestListenerListener(RequestListener l) {
            listener.add(l);
        }

        private void raisePaymentRequest(URI requestUri) {
            for (var l : listener) {
                l.onPaymentRequest(requestUri);
            }
        }
    }
}
