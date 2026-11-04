package com.attendance;

import java.time.Instant;
import java.util.Map;

/** One predictable JSON shape for errors returned by the API. */
public record ApiErrorResponse(Instant timestamp, int status, String error, String message,
                               String path, Map<String, String> fieldErrors) {
}
