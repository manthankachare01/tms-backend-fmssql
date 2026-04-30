package com.tms.restapi.toolsmanagement.exception;

/**
 * Exception thrown when an API resource cannot be found (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
