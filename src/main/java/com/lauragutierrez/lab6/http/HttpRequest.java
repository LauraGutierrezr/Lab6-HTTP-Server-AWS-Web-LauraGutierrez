package com.lauragutierrez.lab6.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses a single HTTP request line (and skips over the header block, which
 * this lab's services never need) into method, path, query parameters and
 * protocol version.
 */
public final class HttpRequest {

    private final String method;
    private final String path;
    private final String rawQuery;
    private final String httpVersion;

    private HttpRequest(String method, String path, String rawQuery, String httpVersion) {
        this.method = method;
        this.path = path;
        this.rawQuery = rawQuery;
        this.httpVersion = httpVersion;
    }

    /**
     * Reads the request line and consumes (without storing) the header
     * lines that follow, up to the blank line that separates headers from
     * a body. Returns {@code null} if the client closed the connection
     * before sending anything (an empty first read).
     *
     * @throws MalformedRequestException if the request line does not have
     *         the expected "METHOD SP target SP version" shape.
     */
    public static HttpRequest parse(BufferedReader reader) throws IOException, MalformedRequestException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank()) {
            return null;
        }

        String[] parts = requestLine.split(" ", 3);
        if (parts.length != 3) {
            throw new MalformedRequestException("Malformed request line: " + requestLine);
        }

        String method = parts[0];
        String target = parts[1];
        String httpVersion = parts[2];

        String path;
        String rawQuery;
        int questionMark = target.indexOf('?');
        if (questionMark >= 0) {
            path = target.substring(0, questionMark);
            rawQuery = target.substring(questionMark + 1);
        } else {
            path = target;
            rawQuery = "";
        }

        // Consume header lines; this lab's services don't need them, but
        // they must still be read off the socket so the connection is left
        // in a clean state.
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            // intentionally discarded
        }

        return new HttpRequest(method, path, rawQuery, httpVersion);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    /** Decodes the query string into a simple name -&gt; value map. */
    public Map<String, String> getQueryParams() {
        Map<String, String> params = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return params;
        }
        for (String pair : rawQuery.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String rawKey = eq >= 0 ? pair.substring(0, eq) : pair;
            String rawValue = eq >= 0 ? pair.substring(eq + 1) : "";
            params.put(decode(rawKey), decode(rawValue));
        }
        return params;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    /** Thrown when the request line cannot be parsed at all. */
    public static final class MalformedRequestException extends Exception {
        public MalformedRequestException(String message) {
            super(message);
        }
    }
}
