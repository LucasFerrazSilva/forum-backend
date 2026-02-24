package com.ferraz.forumbackend.user.exception;

import com.ferraz.forumbackend.infra.exception.InvalidField;
import com.ferraz.forumbackend.infra.exception.ValidationException;

public class NonUniqueEmailException extends ValidationException {

    public NonUniqueEmailException(String email) {
        this(email, "email");
    }

    public NonUniqueEmailException(String email, String fieldName) {
        super(new InvalidField(fieldName, "O email " + email + " já está cadastrado"));
    }

}
