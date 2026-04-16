package com.ferraz.forumbackend.session.exception;

import com.ferraz.forumbackend.infra.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException() {
        super(
                "Erro ao validar as credenciais",
                HttpStatus.UNAUTHORIZED.value(),
                InvalidCredentialsException.class.getSimpleName(),
                "Verifique se o usuário e a senha estão corretos"
        );
    }
}
