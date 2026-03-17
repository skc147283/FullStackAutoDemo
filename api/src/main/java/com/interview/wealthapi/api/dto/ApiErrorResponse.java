package com.interview.wealthapi.api.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorResponse(
        String code,
        String errorCode,
        String message,
        OffsetDateTime timestamp,
        Map<String, Object> details
) {
}
