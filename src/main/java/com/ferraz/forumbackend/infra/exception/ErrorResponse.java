package com.ferraz.forumbackend.infra.exception;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonPropertyOrder({"statusCode", "name", "message", "action", "invalidFields"})
public class ErrorResponse {

    private int statusCode;
    private String name;
    private String message;
    private String action;
    private List<InvalidField> invalidFields;

    public ErrorResponse(int statusCode, String name, String message, String action) {
        this.statusCode = statusCode;
        this.name = name;
        this.message = message;
        this.action = action;
        this.invalidFields = null;
    }

    public ErrorResponse(BaseException exception) {
        this.statusCode = exception.getStatusCode();
        this.name = exception.getName();
        this.message = exception.getMessage();
        this.action = exception.getAction();
        this.invalidFields = exception.getInvalidFields();
    }

}
