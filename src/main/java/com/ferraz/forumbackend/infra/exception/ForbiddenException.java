package com.ferraz.forumbackend.infra.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException() {
        this("Você não tem autorização para consumir esse endpoint");
    }
    public ForbiddenException(String message) {
        super(
                message,
                HttpStatus.FORBIDDEN.value(),
                ForbiddenException.class.getSimpleName(),
                "Entre em contato com o suporte para solicitar a permissão necessária"
        );
    }
}
