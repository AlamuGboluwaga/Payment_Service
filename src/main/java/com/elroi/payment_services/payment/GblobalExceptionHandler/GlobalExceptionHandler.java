package com.elroi.payment_services.payment.GblobalExceptionHandler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException {}", ex.getMessage());

        String message = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).findFirst().orElse("Invalid request");

        ApiErrorResponse response = new ApiErrorResponse(400, "Bad Request", message);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("ConstraintViolationException", ex);

        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .findFirst()
                .orElse("Invalid request");

        ApiErrorResponse response = new ApiErrorResponse(400, "Bad Request", message);
        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(500, "Bad Request", e.getMessage());
        log.error("", e.getMessage());

        return ResponseEntity
                .badRequest()
                .body(apiErrorResponse);
    }

    @ExceptionHandler(DuplicateIdempotencyKey.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {

        ApiErrorResponse response = new ApiErrorResponse(400, "Bad Request", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAmount(
            InvalidAmountException ex) {
        log.error("InvalidAmountException", ex);
        Map<String, String> response = new HashMap<>();

        response.put("message", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}
