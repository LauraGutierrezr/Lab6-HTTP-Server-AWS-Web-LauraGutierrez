package com.lauragutierrez.lab6.util;

/**
 * The lab is deliberately dependency-free, so JSON bodies are built by hand
 * with plain string concatenation. This class is the one place that knows
 * how to escape an untrusted string value so it can never break out of its
 * surrounding quotes or inject additional JSON fields.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    /** Escapes {@code value} and wraps it in double quotes, ready to embed in a JSON document. */
    public static String quote(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
