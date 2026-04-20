package com.ferraz.forumbackend.infra.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException() {
        super(
                "Cookie de sessão não enviado ou inválido",
                HttpStatus.UNAUTHORIZED.value(),
                UnauthorizedException.class.getSimpleName(),
                "Verifique se um cookie válido de sessão está sendo enviado no cabeçalho da requisição"
        );
    }
}
