package com.lauragutierrez.lab6.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContentTypesTest {

    @Test
    void mapsHtmlToTextHtml() {
        assertEquals("text/html; charset=UTF-8", ContentTypes.forFileName("index.html"));
    }

    @Test
    void mapsJsToJavascript() {
        assertEquals("text/javascript; charset=UTF-8", ContentTypes.forFileName("app.js"));
    }

    @Test
    void mapsPngAndJpeg() {
        assertEquals("image/png", ContentTypes.forFileName("logo.png"));
        assertEquals("image/jpeg", ContentTypes.forFileName("diagram.jpg"));
        assertEquals("image/jpeg", ContentTypes.forFileName("diagram.jpeg"));
    }

    @Test
    void isCaseInsensitiveOnTheExtension() {
        assertEquals("image/png", ContentTypes.forFileName("LOGO.PNG"));
    }

    @Test
    void returnsNullForUnknownExtension() {
        assertNull(ContentTypes.forFileName("archive.zip"));
    }

    @Test
    void returnsNullWhenThereIsNoExtensionAtAll() {
        assertNull(ContentTypes.forFileName("README"));
    }
}
