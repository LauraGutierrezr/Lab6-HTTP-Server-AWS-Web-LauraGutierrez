package com.lauragutierrez.lab6.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Small helper to write well-formed HTTP/1.1 responses. Every response is
 * written from a byte array so the Content-Length header always matches the
 * actual bytes sent, whether the body is UTF-8 text or a binary image.
 */
public final class HttpResponses {

    private HttpResponses() {
    }

    public static void send(OutputStream out, int status, String reason,
                             String contentType, byte[] body) throws IOException {
        String headers = "HTTP/1.1 " + status + " " + reason + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";
        out.write(headers.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    public static void sendText(OutputStream out, int status, String reason, String bodyText) throws IOException {
        send(out, status, reason, "text/plain; charset=UTF-8", bodyText.getBytes(StandardCharsets.UTF_8));
    }

    public static void sendJson(OutputStream out, int status, String reason, String json) throws IOException {
        send(out, status, reason, "application/json; charset=UTF-8", json.getBytes(StandardCharsets.UTF_8));
    }

    public static void sendNotFound(OutputStream out, String path) throws IOException {
        sendText(out, 404, "Not Found", "404 Not Found: " + path + " does not exist on this server.");
    }

    public static void sendMethodNotAllowed(OutputStream out, String method) throws IOException {
        sendText(out, 405, "Method Not Allowed",
                "405 Method Not Allowed: " + method + " is not supported by this server. Use GET.");
    }

    public static void sendBadRequest(OutputStream out, String reasonPhrase) throws IOException {
        sendText(out, 400, "Bad Request", "400 Bad Request: " + reasonPhrase);
    }
}
