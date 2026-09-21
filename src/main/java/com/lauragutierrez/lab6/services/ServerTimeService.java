package com.lauragutierrez.lab6.services;

import com.lauragutierrez.lab6.http.ServiceResult;
import com.lauragutierrez.lab6.util.JsonUtil;

import java.time.Instant;

/**
 * Hardcoded server-time service: GET /api/time -&gt; the server's current
 * time. No input, nothing stored between requests - it exists purely to
 * show a value that must come from the remote host, not the browser clock.
 */
public final class ServerTimeService {

    private ServerTimeService() {
    }

    public static ServiceResult handle() {
        Instant now = Instant.now();
        String json = "{\"serverTime\":" + JsonUtil.quote(now.toString())
                + ",\"epochMillis\":" + now.toEpochMilli() + "}";
        return ServiceResult.ok(json);
    }
}
