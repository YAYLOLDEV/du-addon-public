package io.lolyay.addon.dupedb;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.util.Util;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DupeDBOauthLogin {
    private static final String CLIENT_ID = "dupedb-mc-client";
    private static final String REDIRECT_URI = "http%3A%2F%2Flocalhost%3A3000%2Fcallback";
    private static final String OAUTH2_URL = "https://dupedb.net/api/oauth/authorize?app_id=%s&redirect_uri=%s"
        .formatted(CLIENT_ID, REDIRECT_URI);

    private static HttpServer server;
    private static CompletableFuture<String> codeCallback;

    public static CompletableFuture<String> getCode() {
        codeCallback = new CompletableFuture<>();
        startServer();
        Util.getOperatingSystem().open(OAUTH2_URL);
        return codeCallback;
    }

    // MeteorClient login
    @SneakyThrows
    private static void startServer() {
        if (server == null) {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 3000), 0);
            server.createContext("/", new Handler());
            server.setExecutor(MeteorExecutor.executor);
            server.start();

        }
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
            codeCallback = null;
        }
    }

    private static class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange req) throws IOException {
            if (req.getRequestMethod().equals("GET")) {
                // Login
                List<NameValuePair> query = URLEncodedUtils.parse(req.getRequestURI(), StandardCharsets.UTF_8);

                boolean ok = false;

                for (NameValuePair pair : query) {
                    if (pair.getName().equals("code")) {
                        ok = true;
                        codeCallback.complete(pair.getValue());
                    }
                }
                if (!ok) {
                    writeText(req, "Cannot authenticate.");
                    codeCallback.completeExceptionally(new IllegalStateException("Couldnt Find code in User Response!"));
                } else writeText(req, "You may now close this page.");
            }

            stopServer();
        }

        private void writeText(HttpExchange req, String text) throws IOException {
            OutputStream out = req.getResponseBody();

            req.sendResponseHeaders(200, text.length());

            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        }
    }

}
