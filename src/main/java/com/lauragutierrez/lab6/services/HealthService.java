package com.lauragutierrez.lab6.services;

import com.lauragutierrez.lab6.http.ServiceResult;

/**
 * Hardcoded health service: GET /api/health -&gt; a small successful
 * response proving the process can still serve requests.
 */
public final class HealthService {

    private HealthService() {
    }

    public static ServiceResult handle() {
        return ServiceResult.ok("{\"status\":\"UP\"}");
    }
}
