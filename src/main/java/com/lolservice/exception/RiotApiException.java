package com.lolservice.exception;

import lombok.Getter;

/**
 * Exception thrown when there is an error with the Riot API.
 */
@Getter
public class RiotApiException extends RuntimeException {
    private final int status;

    public RiotApiException(String message) {
        super(message);
        this.status = 500; // 기본 상태 코드
    }

    public RiotApiException(String message, int statusCode) {
        super(message + " (Status: " + statusCode + ")");
        this.status = statusCode;
    }

    public RiotApiException(String message, Throwable cause) {
        super(message, cause);
        this.status = 500; // 기본 상태 코드
    }

    public RiotApiException(Throwable cause) {
        super(cause);
        this.status = 500; // 기본 상태 코드
    }
}