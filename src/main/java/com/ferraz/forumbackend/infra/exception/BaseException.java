package com.ferraz.forumbackend.infra.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException {

    private final int statusCode;
    private final String name;
    private final String message;
    private final String action;

    public BaseException(String message, int statusCode, String name, String action, Exception cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.name = name;
        this.message = message;
        this.action = action;
    }

}
