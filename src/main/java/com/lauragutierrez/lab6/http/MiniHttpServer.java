package com.lauragutierrez.lab6.http;

import com.lauragutierrez.lab6.services.GreetingService;
import com.lauragutierrez.lab6.services.HealthService;
import com.lauragutierrez.lab6.services.ServerTimeService;
import com.lauragutierrez.lab6.services.SquareService;
import com.lauragutierrez.lab6.util.PathUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sequential HTTP server: static resources (HTML, JavaScript, images) plus
 * a small, explicitly hardcoded set of dynamic service URLs.
 *
 * <p>Routing here is deliberately a flat if/else chain over exact path
 * strings - no framework, no reflection, no annotations. That is the point
 * of this stage of the lab: the mechanism that picks behavior from a path
 * must stay visible.
 *
 * <p>Still no concurrency: the accept() loop handles one client socket at a
 * time, from the first byte of the request to the last byte of the
 * response, before moving on to the next connection.
 */
public final class MiniHttpServer {

    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_PUBLIC_DIR = "public";

    public static void main(String[] args) throws IOException {
        int port = resolvePort(args);
        Path publicRoot = resolvePublicRoot();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Mini HTTP Server listening on port " + port);
            System.out.println("Serving static resources from: " + publicRoot);
            serve(serverSocket, publicRoot);
        }
    }

    /**
     * The sequential accept loop, extracted from {@link #main} so tests can
     * run it against an ephemeral, in-process ServerSocket and stop it
     * cleanly by closing that socket.
     */
    static void serve(ServerSocket serverSocket, Path publicRoot) {
        while (!serverSocket.isClosed()) {
            try (Socket client = serverSocket.accept()) {
                handleConnection(client, publicRoot);
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    // The socket was closed (e.g. shutdown) while accept()
                    // was blocked; that is an expected way to stop, not an
                    // error to report.
                    break;
                }
                System.err.println("Error handling connection: " + e.getMessage());
            }
        }
    }

    static void handleConnection(Socket client, Path publicRoot) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = client.getOutputStream();

        HttpRequest request;
        try {
            request = HttpRequest.parse(in);
        } catch (HttpRequest.MalformedRequestException e) {
            HttpResponses.sendBadRequest(out, e.getMessage());
            return;
        }

        if (request == null) {
            // Client opened the connection and closed it without sending
            // a request; nothing to answer.
            return;
        }

        System.out.println(request.getMethod() + " " + request.getPath());

        if (!"GET".equals(request.getMethod())) {
            HttpResponses.sendMethodNotAllowed(out, request.getMethod());
            return;
        }

        dispatch(out, publicRoot, request);
    }

    /**
     * The entire routing table for this lab: a handful of explicit,
     * hardcoded paths for dynamic services, and everything else falls
     * through to static resource serving.
     */
    private static void dispatch(OutputStream out, Path publicRoot, HttpRequest request) throws IOException {
        String path = request.getPath();

        ServiceResult result = switch (path) {
            case "/api/greet" -> GreetingService.handle(request.getQueryParams());
            case "/api/square" -> SquareService.handle(request.getQueryParams());
            case "/api/time" -> ServerTimeService.handle();
            case "/api/health" -> HealthService.handle();
            default -> null;
        };

        if (result != null) {
            HttpResponses.sendJson(out, result.status(), result.reason(), result.json());
            return;
        }

        serveStaticResource(out, publicRoot, path);
    }

    private static void serveStaticResource(OutputStream out, Path publicRoot, String rawPath) throws IOException {
        String decodedPath = PathUtil.decodePath(rawPath);
        Path resolved = PathUtil.resolveInsidePublicRoot(publicRoot, decodedPath);

        if (resolved == null || !Files.isRegularFile(resolved)) {
            HttpResponses.sendNotFound(out, rawPath);
            return;
        }

        String contentType = ContentTypes.forFileName(resolved.getFileName().toString());
        if (contentType == null) {
            // Unknown extension: treated the same as "missing" per the lab spec.
            HttpResponses.sendNotFound(out, rawPath);
            return;
        }

        byte[] body = Files.readAllBytes(resolved);
        HttpResponses.send(out, 200, "OK", contentType, body);
    }

    private static int resolvePort(String[] args) {
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            return Integer.parseInt(envPort);
        }
        return DEFAULT_PORT;
    }

    private static Path resolvePublicRoot() throws IOException {
        String configured = System.getenv("PUBLIC_DIR");
        String dir = (configured != null && !configured.isBlank()) ? configured : DEFAULT_PUBLIC_DIR;
        Path path = Path.of(dir);
        if (!Files.isDirectory(path)) {
            throw new IOException("Public resources directory not found: " + path.toAbsolutePath()
                    + ". Run the server from the directory that contains it, "
                    + "or set the PUBLIC_DIR environment variable.");
        }
        return path.toRealPath();
    }
}
