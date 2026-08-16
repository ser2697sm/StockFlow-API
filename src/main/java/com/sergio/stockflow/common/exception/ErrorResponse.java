package com.sergio.stockflow.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        int status,
        LocalDateTime timestamp,
        String correlationId,
        Map<String, String> errors,
        String path
) {
}
