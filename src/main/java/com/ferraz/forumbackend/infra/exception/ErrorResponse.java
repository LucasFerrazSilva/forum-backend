package com.ferraz.forumbackend.infra.exception;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@JsonPropertyOrder({"statusCode", "name", "message", "action"})
public class ErrorResponse {

    private final int statusCode;
    private final String name;
    private final String message;
    private final String action;

    public ErrorResponse(BaseException exception) {
        this.statusCode = exception.getStatusCode();
        this.name = exception.getName();
        this.message = exception.getMessage();
        this.action = exception.getAction();
    }

}
