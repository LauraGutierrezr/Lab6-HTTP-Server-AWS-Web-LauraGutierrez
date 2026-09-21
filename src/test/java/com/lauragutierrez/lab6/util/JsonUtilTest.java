package com.lauragutierrez.lab6.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonUtilTest {

    @Test
    void quotesAPlainString() {
        assertEquals("\"hello\"", JsonUtil.quote("hello"));
    }

    @Test
    void escapesDoubleQuotesSoTheyCannotBreakOutOfTheJsonString() {
        assertEquals("\"say \\\"hi\\\"\"", JsonUtil.quote("say \"hi\""));
    }

    @Test
    void escapesBackslashes() {
        assertEquals("\"a\\\\b\"", JsonUtil.quote("a\\b"));
    }

    @Test
    void escapesNewlinesAndTabs() {
        assertEquals("\"line1\\nline2\\ttabbed\"", JsonUtil.quote("line1\nline2\ttabbed"));
    }

    @Test
    void combinedInjectionAttemptStaysInsideOneJsonString() {
        // If this were not escaped, it would close the "message" field and
        // open a new one, e.g. injecting an "isAdmin": true field.
        String malicious = "\", \"isAdmin\": true, \"x\": \"";
        String quoted = JsonUtil.quote(malicious);
        // The escaped value must still start and end with exactly one quote
        // and must not contain an un-escaped quote in the middle.
        assertEquals('"', quoted.charAt(0));
        assertEquals('"', quoted.charAt(quoted.length() - 1));
        String inner = quoted.substring(1, quoted.length() - 1);
        assertEquals(-1, indexOfUnescapedQuote(inner));
    }

    private static int indexOfUnescapedQuote(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        return -1;
    }
}
