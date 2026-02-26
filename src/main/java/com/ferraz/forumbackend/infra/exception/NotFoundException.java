package com.ferraz.forumbackend.infra.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(
                message,
                HttpStatus.NOT_FOUND.value(),
                NotFoundException.class.getSimpleName(),
                "Revise os valores enviados"
        );
    }
}
