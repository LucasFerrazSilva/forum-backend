package com.ferraz.forumbackend.user.exception;

import com.ferraz.forumbackend.infra.exception.InvalidField;
import com.ferraz.forumbackend.infra.exception.ValidationException;

public class NonUniqueUsernameException extends ValidationException {

    public NonUniqueUsernameException(String username) {
        this(username, "username");
    }

    public NonUniqueUsernameException(String username, String fieldName) {
        super(new InvalidField(fieldName, "O username %s já está cadastrado".formatted(username)));
    }

}
