package com.ferraz.forumbackend.activationtoken.exception;

import com.ferraz.forumbackend.infra.exception.NotFoundException;

import java.util.UUID;

public class InvalidActivationTokenException extends NotFoundException {
    public InvalidActivationTokenException(UUID id) {
        super("Código de ativação inválido: " + id);
    }
}
