package com.healthcard.backend.exception;

/** Thrown when a doctor/pathologist tries to access a patient record without valid authorization. */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
