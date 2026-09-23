package com.powerfitness.exception;

import org.springframework.http.HttpStatus;

/** 401 — authentication failed (bad email/password or invalid/expired token). */
public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", message);
    }
}
