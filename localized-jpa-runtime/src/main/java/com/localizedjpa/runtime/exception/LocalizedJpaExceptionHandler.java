package com.localizedjpa.runtime.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for localized JPA exceptions.
 */
@RestControllerAdvice
public class LocalizedJpaExceptionHandler {

    @ExceptionHandler(UnsupportedLocaleException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedLocale(UnsupportedLocaleException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_ACCEPTABLE.value());
        body.put("error", "Not Acceptable");
        body.put("message", ex.getMessage());
        body.put("requestedLocale", ex.getRequestedLocale());
        body.put("defaultLocale", ex.getDefaultLocale());
        
        return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);
    }
}
