package com.lauragutierrez.lab6.services;

import com.lauragutierrez.lab6.http.ServiceResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthAndTimeServiceTest {

    @Test
    void healthAlwaysReturnsOkWithAFixedBody() {
        ServiceResult result = HealthService.handle();
        assertEquals(200, result.status());
        assertEquals("{\"status\":\"UP\"}", result.json());
    }

    @Test
    void serverTimeReturnsAnIsoTimestampAndEpochMillis() {
        long before = System.currentTimeMillis();
        ServiceResult result = ServerTimeService.handle();
        long after = System.currentTimeMillis();

        assertEquals(200, result.status());
        assertTrue(result.json().contains("serverTime"));
        assertTrue(result.json().contains("epochMillis"));

        String json = result.json();
        long epochMillis = Long.parseLong(
                json.replaceAll(".*\"epochMillis\":(\\d+)\\}", "$1"));
        assertTrue(epochMillis >= before && epochMillis <= after,
                "server time should be within the test's execution window");
    }
}
