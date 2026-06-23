package com.healthcard.backend.exception;

/** Thrown when a doctor/pathologist account has not yet been verified by an admin. */
public class AccountNotVerifiedException extends RuntimeException {
    public AccountNotVerifiedException(String message) {
        super(message);
    }
}
