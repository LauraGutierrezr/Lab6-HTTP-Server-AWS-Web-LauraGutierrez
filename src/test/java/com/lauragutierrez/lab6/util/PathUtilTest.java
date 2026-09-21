package com.lauragutierrez.lab6.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PathUtilTest {

    @TempDir
    Path tempDir;

    Path publicRoot;

    @BeforeEach
    void setUp() throws IOException {
        publicRoot = tempDir.resolve("public");
        Files.createDirectories(publicRoot.resolve("images"));
        Files.writeString(publicRoot.resolve("index.html"), "<html></html>");
        Files.writeString(publicRoot.resolve("app.js"), "console.log(1);");
        Files.writeString(publicRoot.resolve("images").resolve("logo.png"), "not-a-real-png");
        // A "secret" file OUTSIDE the public root, which a traversal
        // attempt must never be able to reach.
        Files.writeString(tempDir.resolve("secret.txt"), "top secret");
        publicRoot = publicRoot.toRealPath();
    }

    @Test
    void rootPathMapsToIndexHtml() {
        Path resolved = PathUtil.resolveInsidePublicRoot(publicRoot, "/");
        assertEquals(publicRoot.resolve("index.html"), resolved);
    }

    @Test
    void resolvesAnOrdinaryNestedResource() {
        Path resolved = PathUtil.resolveInsidePublicRoot(publicRoot, "/images/logo.png");
        assertEquals(publicRoot.resolve("images").resolve("logo.png"), resolved);
    }

    @Test
    void rejectsDotDotTraversalOutsidePublicRoot() {
        Path resolved = PathUtil.resolveInsidePublicRoot(publicRoot, "/../secret.txt");
        assertNull(resolved);
    }

    @Test
    void rejectsDeeperDotDotTraversal() {
        Path resolved = PathUtil.resolveInsidePublicRoot(publicRoot, "/images/../../secret.txt");
        assertNull(resolved);
    }

    @Test
    void rejectsPathsThatDoNotStartWithASlash() {
        Path resolved = PathUtil.resolveInsidePublicRoot(publicRoot, "images/logo.png");
        assertNull(resolved);
    }

    @Test
    void decodesPercentEncodedCharacters() {
        assertEquals("/images/logo.png", PathUtil.decodePath("/images/logo.png"));
        assertEquals("/a b.html", PathUtil.decodePath("/a%20b.html"));
    }

    @Test
    void percentEncodedTraversalIsAlsoRejectedAfterDecoding() {
        // "%2e%2e" decodes to ".."
        String decoded = PathUtil.decodePath("/%2e%2e/secret.txt");
        Path resolved = PathUtil.resolveInsidePublicRoot(publicRoot, decoded);
        assertNull(resolved);
    }
}
