package org.example.lufthansaurlsystem.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, String> error = new HashMap<>();

        if (ex.getMessage().contains("URL has expired")) {
            error.put("error", "URL expired");
            error.put("message", "The requested URL has expired and been deleted");
            error.put("status", "410");
            return ResponseEntity.status(HttpStatus.GONE).body(error);
        }

        if (ex.getMessage().contains("URL not found")) {
            error.put("error", "URL not found");
            error.put("message", "The requested short URL does not exist");
            error.put("status", "404");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        if (ex.getMessage().contains("Database connection failed")) {
            error.put("error", "Service unavailable");
            error.put("message", "Database is temporarily unavailable");
            error.put("status", "503");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }

        error.put("error", "Runtime error");
        error.put("message", ex.getMessage());
        error.put("status", "500");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
