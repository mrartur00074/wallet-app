package com.example.walletapp.exception;

import com.example.walletapp.dto.response.ErrorResponse;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageConversion(HttpMessageConversionException ex) {
        String message = "Ошибка в формате данных: " + ex.getCause().getMessage();
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(message, "INVALID_JSON", 400));
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(TypeMismatchException ex) {
        String message = "Некорректный формат UUID. Пример: 123e4567-e89b-12d3-a456-426614174000";
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(message, "INVALID_UUID", 400));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        "Validation error: " + errorMessage,
                        "VALIDATION_FAILED",
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        ex.getMessage(),
                        "ILLEGAL_STATE",
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    @ExceptionHandler(WalletException.class)
    public ResponseEntity<ErrorResponse> handleWalletExceptions(WalletException ex) {
        HttpStatus status = determineHttpStatus(ex);

        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        getErrorCode(ex),
                        status.value()
                ));
    }

    private HttpStatus determineHttpStatus(WalletException ex) {
        if (ex instanceof WalletNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof InvalidOperationException) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private String getErrorCode(WalletException ex) {
        if (ex instanceof WalletNotFoundException) {
            return "WALLET_NOT_FOUND";
        } else if (ex instanceof InsufficientFundsException) {
            return "INSUFFICIENT_FUNDS";
        } else if (ex instanceof InvalidOperationException) {
            return "INVALID_OPERATION";
        }
        return "INTERNAL_ERROR";
    }
}