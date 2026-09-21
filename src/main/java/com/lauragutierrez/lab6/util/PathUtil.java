package com.lauragutierrez.lab6.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Small helpers for turning a raw HTTP request path into a safe file inside
 * the public-resources directory.
 *
 * <p>Every request path is percent-decoded and normalized before it is
 * resolved against the public root, and the resolved, real, absolute path
 * must still be located inside that root. This is what stops a request
 * like {@code /../../etc/passwd} (or its percent-encoded form) from ever
 * reaching a file outside the public-resources area.
 */
public final class PathUtil {

    private PathUtil() {
    }

    /**
     * Percent-decodes a request path. Unlike {@link URLDecoder#decode}, a
     * literal '+' is left untouched (it is only special in query strings,
     * not in paths).
     */
    public static String decodePath(String rawPath) {
        try {
            String placeholderSafe = rawPath.replace("+", "%2B");
            return URLDecoder.decode(placeholderSafe, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            // Malformed percent-encoding: treat as "no safe path" so the
            // caller can respond with a controlled error instead of
            // guessing at a partially-decoded path.
            return null;
        }
    }

    /**
     * Resolves {@code requestPath} (already percent-decoded) against
     * {@code publicRoot}, returning {@code null} if the result would fall
     * outside the public-resources directory or the path is otherwise
     * unusable.
     */
    public static Path resolveInsidePublicRoot(Path publicRoot, String requestPath) {
        if (requestPath == null || !requestPath.startsWith("/")) {
            return null;
        }

        String relative = "/".equals(requestPath) ? "index.html" : requestPath.substring(1);

        // Reject embedded NUL bytes outright; some filesystems/APIs choke
        // on them in unpredictable ways.
        if (relative.indexOf('\0') >= 0) {
            return null;
        }

        Path candidate = publicRoot.resolve(relative).normalize();

        if (!candidate.startsWith(publicRoot)) {
            return null;
        }
        return candidate;
    }
}
