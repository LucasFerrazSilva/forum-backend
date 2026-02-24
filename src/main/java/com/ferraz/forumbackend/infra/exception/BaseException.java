package com.ferraz.forumbackend.infra.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException {

    private final int statusCode;
    private final String name;
    private final String message;
    private final String action;
    private final List<InvalidField> invalidFields;

    public BaseException(String message, int statusCode, String name, String action) {
        this(message, statusCode, name, action, null, null);
    }

    public BaseException(String message, int statusCode, String name, String action, InvalidField invalidField) {
        this(message, statusCode, name, action, null, List.of(invalidField));
    }

    public BaseException(String message, int statusCode, String name, String action, List<InvalidField> invalidFields) {
        this(message, statusCode, name, action, null, invalidFields);
    }

    public BaseException(String message, int statusCode, String name, String action, Exception cause) {
        this(message, statusCode, name, action, cause, null);
    }

    public BaseException(String message, int statusCode, String name, String action, Exception cause, List<InvalidField> invalidFields) {
        super(message, cause);
        this.statusCode = statusCode;
        this.name = name;
        this.message = message;
        this.action = action;
        this.invalidFields = invalidFields;
    }

}
