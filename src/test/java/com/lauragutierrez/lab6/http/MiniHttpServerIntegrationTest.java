package com.lauragutierrez.lab6.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests that run the real sequential server against a real
 * socket, on an ephemeral port, over an in-process HTTP client. This is
 * what proves the whole request/response path works, not just the
 * individual helper classes.
 */
class MiniHttpServerIntegrationTest {

    private ServerSocket serverSocket;
    private Thread serverThread;
    private int port;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    @BeforeEach
    void startServer(@TempDir Path tempDir) throws IOException {
        Path publicRoot = tempDir.resolve("public");
        Files.createDirectories(publicRoot);
        Files.writeString(publicRoot.resolve("index.html"), "<html><body>home</body></html>");
        Files.writeString(publicRoot.resolve("app.js"), "console.log('ok');");

        serverSocket = new ServerSocket(0); // 0 = let the OS pick a free port
        port = serverSocket.getLocalPort();
        Path realPublicRoot = publicRoot.toRealPath();

        serverThread = new Thread(() -> MiniHttpServer.serve(serverSocket, realPublicRoot));
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @AfterEach
    void stopServer() throws IOException, InterruptedException {
        serverSocket.close();
        serverThread.join(2000);
    }

    private HttpResponse<String> get(String path) throws Exception {
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void servesTheHomePage() throws Exception {
        HttpResponse<String> response = get("/");
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
        assertTrue(response.body().contains("home"));
    }

    @Test
    void servesJavaScriptWithTheRightContentType() throws Exception {
        HttpResponse<String> response = get("/app.js");
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("text/javascript"));
    }

    @Test
    void missingResourceReturnsNotFound() throws Exception {
        HttpResponse<String> response = get("/does-not-exist.html");
        assertEquals(404, response.statusCode());
    }

    @Test
    void pathTraversalAttemptDoesNotLeakFiles() throws Exception {
        HttpResponse<String> response = get("/../../../../etc/passwd");
        assertEquals(404, response.statusCode());
        assertTrue(response.body().toLowerCase().indexOf("root:") < 0);
    }

    @Test
    void greetingServiceReturnsJson() throws Exception {
        HttpResponse<String> response = get("/api/greet?name=Laura");
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertTrue(response.body().contains("Laura"));
    }

    @Test
    void greetingServiceRejectsMissingName() throws Exception {
        HttpResponse<String> response = get("/api/greet");
        assertEquals(400, response.statusCode());
    }

    @Test
    void squareServiceComputesTheSquare() throws Exception {
        HttpResponse<String> response = get("/api/square?value=6");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("36"));
    }

    @Test
    void healthServiceReturnsUp() throws Exception {
        HttpResponse<String> response = get("/api/health");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("UP"));
    }

    @Test
    void postIsRejectedAsMethodNotAllowed() throws Exception {
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/"))
                .POST(BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode());
    }

    @Test
    void handlesTenConsecutiveRequestsOnTheSameServerRun() throws Exception {
        for (int i = 0; i < 10; i++) {
            HttpResponse<String> response = get("/api/health");
            assertEquals(200, response.statusCode(), "request #" + i + " should succeed");
        }
    }
}
