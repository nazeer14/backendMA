package com.pack.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Standardized error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

        @Schema(description = "HTTP status code")
        int status,

        @Schema(description = "Error type")
        String error,

        @Schema(description = "Human-readable error message")
        String message,

        @Schema(description = "Request path that triggered the error")
        String path,

        @Schema(description = "Timestamp of the error")
        OffsetDateTime timestamp,

        @Schema(description = "Field-level validation errors")
        List<FieldError> fieldErrors
) {
    public record FieldError(
            @Schema(description = "Field name that failed validation")
            String field,

            @Schema(description = "Rejected value")
            Object rejectedValue,

            @Schema(description = "Validation error message")
            String message
    ) {}

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, OffsetDateTime.now(), null);
    }

    public static ErrorResponse of(int status, String error, String message, String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(status, error, message, path, OffsetDateTime.now(), fieldErrors);
    }
}