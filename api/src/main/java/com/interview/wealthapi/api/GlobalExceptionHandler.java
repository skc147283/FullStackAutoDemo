package com.interview.wealthapi.api;

import com.interview.wealthapi.api.dto.ApiErrorResponse;
import com.interview.wealthapi.exception.BusinessException;
import com.interview.wealthapi.exception.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        "NOT_FOUND",
                        mapNotFoundErrorCode(ex.getMessage()),
                        ex.getMessage(),
                        OffsetDateTime.now(),
                        Map.of()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiErrorResponse(
                        "BUSINESS_RULE",
                        mapBusinessErrorCode(ex.getMessage()),
                        ex.getMessage(),
                        OffsetDateTime.now(),
                        Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError firstFieldError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);

        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error instanceof FieldError fieldError
                        ? fieldError.getField() + " " + fieldError.getDefaultMessage()
                        : error.getDefaultMessage())
                .orElse("Validation failed");

        Map<String, Object> details;
        if (firstFieldError == null) {
            details = Map.of();
        } else {
            Object rejectedValue = firstFieldError.getRejectedValue();
            details = Map.of(
                "field", firstFieldError.getField(),
                "rejectedValue", rejectedValue == null ? "null" : String.valueOf(rejectedValue));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        "VALIDATION_ERROR",
                        mapValidationErrorCode(message),
                        message,
                        OffsetDateTime.now(),
                        details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        "INTERNAL_ERROR",
                        "INTERNAL_SERVER_ERROR",
                        "Something went wrong. Please try again.",
                        OffsetDateTime.now(),
                        Map.of()));
    }

    private String mapNotFoundErrorCode(String message) {
        if (message == null) {
            return "RESOURCE_NOT_FOUND";
        }
        String normalized = message.toLowerCase();
        if (normalized.contains("customer")) {
            return "CUSTOMER_NOT_FOUND";
        }
        if (normalized.contains("account")) {
            return "ACCOUNT_NOT_FOUND";
        }
        return "RESOURCE_NOT_FOUND";
    }

    private String mapBusinessErrorCode(String message) {
        if (message == null) {
            return "BUSINESS_RULE_VIOLATION";
        }
        String normalized = message.toLowerCase();
        if (normalized.contains("amount must be greater than zero")) {
            return "INVALID_AMOUNT";
        }
        if (normalized.contains("opening balance") && normalized.contains("negative")) {
            return "NEGATIVE_OPENING_BALANCE";
        }
        if (normalized.contains("source and destination") && normalized.contains("different")) {
            return "SAME_ACCOUNT_TRANSFER";
        }
        if (normalized.contains("currency mismatch")) {
            return "CURRENCY_MISMATCH";
        }
        if (normalized.contains("insufficient balance")) {
            return "INSUFFICIENT_BALANCE";
        }
        return "BUSINESS_RULE_VIOLATION";
    }

    private String mapValidationErrorCode(String message) {
        if (message == null) {
            return "INVALID_INPUT";
        }
        String normalized = message.toLowerCase();
        if (normalized.contains("fullName".toLowerCase())) {
            return "INVALID_CUSTOMER_NAME";
        }
        if (normalized.contains("email")) {
            return "INVALID_EMAIL";
        }
        if (normalized.contains("riskProfile".toLowerCase())) {
            return "INVALID_RISK_PROFILE";
        }
        if (normalized.contains("amount")) {
            return "INVALID_AMOUNT";
        }
        return "INVALID_INPUT";
    }
}
