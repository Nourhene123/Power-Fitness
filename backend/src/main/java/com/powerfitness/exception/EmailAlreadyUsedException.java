package com.powerfitness.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyUsedException extends ApiException {

    public EmailAlreadyUsedException(String email) {
        super(HttpStatus.CONFLICT, "EMAIL_ALREADY_USED",
                "An account with %s already exists".formatted(email));
    }
}
