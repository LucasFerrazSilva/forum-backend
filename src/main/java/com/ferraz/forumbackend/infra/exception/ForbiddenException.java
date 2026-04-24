package com.ferraz.forumbackend.infra.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException() {
        super(
                "Você não tem autorização para consumir esse endpoint",
                HttpStatus.FORBIDDEN.value(),
                ForbiddenException.class.getSimpleName(),
                "Verifique se o seu usuário tem as permissões necessárias"
        );
    }
}
