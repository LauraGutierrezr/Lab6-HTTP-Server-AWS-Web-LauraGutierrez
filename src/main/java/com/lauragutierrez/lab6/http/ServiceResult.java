package com.lauragutierrez.lab6.http;

/**
 * The outcome of a hardcoded service call: an HTTP status/reason pair and
 * the JSON body to send back. Every hardcoded service in this lab (valid or
 * invalid input) resolves to one of these.
 */
public record ServiceResult(int status, String reason, String json) {

    public static ServiceResult ok(String json) {
        return new ServiceResult(200, "OK", json);
    }

    public static ServiceResult badRequest(String message) {
        String json = "{\"error\":" + com.lauragutierrez.lab6.util.JsonUtil.quote(message) + "}";
        return new ServiceResult(400, "Bad Request", json);
    }
}
