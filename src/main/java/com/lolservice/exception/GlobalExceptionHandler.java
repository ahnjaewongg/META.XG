package com.lolservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error occurred: ", e);
        return ResponseEntity
            .internalServerError()
            .body(new ErrorResponse("Internal Server Error", e.getMessage()));
    }

    @ExceptionHandler(RiotApiException.class)
    public ResponseEntity<ErrorResponse> handleRiotApiException(RiotApiException e) {
        log.error("Riot API error: ", e);
        return ResponseEntity
            .status(e.getStatus())
            .body(new ErrorResponse("Riot API Error", e.getMessage()));
    }
}

record ErrorResponse(String error, String message) {} 