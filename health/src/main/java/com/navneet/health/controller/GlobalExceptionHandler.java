package com.navneet.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleParseError(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body("Invalid request data: " + e.getMessage());
    }
}