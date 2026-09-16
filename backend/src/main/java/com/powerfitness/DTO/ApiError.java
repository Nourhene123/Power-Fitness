package com.powerfitness.DTO;

import java.time.Instant;
import java.util.List;


public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<FieldError> fieldErrors) {

    public record FieldError(String field, String message) {}

    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, path, List.of());
    }

    public static ApiError validation(String message, String path, List<FieldError> fieldErrors) {
        return new ApiError(Instant.now(), 400, "VALIDATION_FAILED", message, path, fieldErrors);
    }
}
