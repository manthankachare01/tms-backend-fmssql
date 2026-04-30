package com.tms.restapi.toolsmanagement.exception;

/**
 * Exception to indicate a bad request (HTTP 400) in API processing.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
} 
