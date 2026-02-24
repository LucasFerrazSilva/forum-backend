package com.ferraz.forumbackend.infra.exception;

import org.springframework.validation.FieldError;

public record InvalidField(String field, String message) {
    public InvalidField(FieldError fieldError) {
        this(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
