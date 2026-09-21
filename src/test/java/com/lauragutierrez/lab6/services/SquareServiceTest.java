package com.lauragutierrez.lab6.services;

import com.lauragutierrez.lab6.http.ServiceResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SquareServiceTest {

    @Test
    void squaresAWholeNumber() {
        ServiceResult result = SquareService.handle(Map.of("value", "7"));
        assertEquals(200, result.status());
        assertEquals("{\"input\":7,\"square\":49}", result.json());
    }

    @Test
    void squaresANegativeNumber() {
        ServiceResult result = SquareService.handle(Map.of("value", "-3"));
        assertEquals(200, result.status());
        assertEquals("{\"input\":-3,\"square\":9}", result.json());
    }

    @Test
    void squaresADecimalValue() {
        ServiceResult result = SquareService.handle(Map.of("value", "2.5"));
        assertEquals(200, result.status());
        assertEquals("{\"input\":2.5,\"square\":6.25}", result.json());
    }

    @Test
    void missingValueIsABadRequest() {
        ServiceResult result = SquareService.handle(Map.of());
        assertEquals(400, result.status());
        assertTrue(result.json().contains("required"));
    }

    @Test
    void blankValueIsABadRequest() {
        ServiceResult result = SquareService.handle(Map.of("value", "   "));
        assertEquals(400, result.status());
    }

    @Test
    void nonNumericValueIsABadRequest() {
        ServiceResult result = SquareService.handle(Map.of("value", "not-a-number"));
        assertEquals(400, result.status());
        assertTrue(result.json().contains("error"));
    }
}
