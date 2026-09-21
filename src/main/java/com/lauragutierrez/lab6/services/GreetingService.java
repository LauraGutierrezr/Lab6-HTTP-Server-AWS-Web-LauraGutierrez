package com.lauragutierrez.lab6.services;

import com.lauragutierrez.lab6.http.ServiceResult;
import com.lauragutierrez.lab6.util.JsonUtil;

import java.util.Map;

/**
 * Hardcoded greeting service: GET /api/greet?name=... -&gt; a JSON greeting.
 *
 * <p>The supplied name is never interpolated into the JSON body without
 * going through {@link JsonUtil#quote}, so a name containing quotes,
 * backslashes or control characters cannot break out of the JSON structure.
 */
public final class GreetingService {

    private GreetingService() {
    }

    public static ServiceResult handle(Map<String, String> queryParams) {
        String rawName = queryParams.get("name");
        if (rawName == null || rawName.isBlank()) {
            return ServiceResult.badRequest("Query parameter 'name' is required and cannot be blank.");
        }

        String name = rawName.strip();
        String message = "Hello, " + name + "!";

        String json = "{\"name\":" + JsonUtil.quote(name)
                + ",\"message\":" + JsonUtil.quote(message) + "}";
        return ServiceResult.ok(json);
    }
}
