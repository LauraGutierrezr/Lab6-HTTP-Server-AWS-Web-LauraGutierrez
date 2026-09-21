package com.lauragutierrez.lab6.http;

import java.util.Locale;
import java.util.Map;

/**
 * Explicit, hardcoded mapping between file extensions and HTTP content
 * types. On purpose this is a flat map, not a pluggable registry: the lab
 * only needs to recognize a handful of static resource types.
 */
public final class ContentTypes {

    private static final Map<String, String> BY_EXTENSION = Map.of(
            "html", "text/html; charset=UTF-8",
            "htm", "text/html; charset=UTF-8",
            "js", "text/javascript; charset=UTF-8",
            "css", "text/css; charset=UTF-8",
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg"
    );

    private ContentTypes() {
    }

    /**
     * @return the content type for the given file name's extension, or
     *         {@code null} when the extension is missing or not one of the
     *         types this server is willing to serve.
     */
    public static String forFileName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return BY_EXTENSION.get(extension);
    }
}
