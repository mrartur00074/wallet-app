package com.example.walletapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(WalletException.class)
    public ResponseEntity<Map<String, String>> handleWalletExceptions(WalletException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        HttpStatus status = determineHttpStatus(ex);
        return new ResponseEntity<>(error, status);
    }

    private HttpStatus determineHttpStatus(WalletException ex) {
        if (ex instanceof WalletNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof InsufficientFundsException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof InvalidOperationException) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}