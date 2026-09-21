package com.lauragutierrez.lab6.services;

import com.lauragutierrez.lab6.http.ServiceResult;

import java.util.Map;

/**
 * Hardcoded square service: GET /api/square?value=... -&gt; the value and
 * its square as JSON. Mirrors the earlier socket exercise this lab builds
 * on top of.
 */
public final class SquareService {

    private SquareService() {
    }

    public static ServiceResult handle(Map<String, String> queryParams) {
        String raw = queryParams.get("value");
        if (raw == null || raw.isBlank()) {
            return ServiceResult.badRequest("Query parameter 'value' is required and must be a number.");
        }

        double value;
        try {
            value = Double.parseDouble(raw.strip());
        } catch (NumberFormatException e) {
            return ServiceResult.badRequest("Query parameter 'value' must be a valid number, got: " + raw);
        }

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return ServiceResult.badRequest("Query parameter 'value' must be a finite number.");
        }

        double square = value * value;
        String json = "{\"input\":" + formatNumber(value) + ",\"square\":" + formatNumber(square) + "}";
        return ServiceResult.ok(json);
    }

    private static String formatNumber(double value) {
        // Whole numbers print without a trailing ".0" for a cleaner JSON
        // body (4 instead of 4.0), everything else keeps its decimals.
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
