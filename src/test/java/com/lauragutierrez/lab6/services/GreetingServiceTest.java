package com.lauragutierrez.lab6.services;

import com.lauragutierrez.lab6.http.ServiceResult;
import com.lauragutierrez.lab6.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingServiceTest {

    @Test
    void greetsAGivenName() {
        ServiceResult result = GreetingService.handle(Map.of("name", "Laura"));
        assertEquals(200, result.status());
        assertEquals("{\"name\":\"Laura\",\"message\":\"Hello, Laura!\"}", result.json());
    }

    @Test
    void trimsSurroundingWhitespace() {
        ServiceResult result = GreetingService.handle(Map.of("name", "  Alvaro  "));
        assertEquals("{\"name\":\"Alvaro\",\"message\":\"Hello, Alvaro!\"}", result.json());
    }

    @Test
    void missingNameIsABadRequest() {
        ServiceResult result = GreetingService.handle(Map.of());
        assertEquals(400, result.status());
    }

    @Test
    void blankNameIsABadRequest() {
        ServiceResult result = GreetingService.handle(Map.of("name", "   "));
        assertEquals(400, result.status());
    }

    @Test
    void aNameContainingQuotesIsSafelyEscapedInTheJsonBody() {
        String maliciousName = "\"; DROP";
        ServiceResult result = GreetingService.handle(Map.of("name", maliciousName));
        assertEquals(200, result.status());

        String expected = "{\"name\":" + JsonUtil.quote(maliciousName)
                + ",\"message\":" + JsonUtil.quote("Hello, " + maliciousName + "!") + "}";
        assertEquals(expected, result.json());
    }
}
