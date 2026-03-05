package com.ferraz.forumbackend.user.exception;

import com.ferraz.forumbackend.infra.exception.NotFoundException;

public class UsernameNotFoundException extends NotFoundException {

    public UsernameNotFoundException(String username) {
        super("Nenhum usuário encontrado para o username " + username);
    }

}
