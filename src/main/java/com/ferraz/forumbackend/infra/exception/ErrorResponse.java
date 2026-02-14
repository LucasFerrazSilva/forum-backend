package com.ferraz.forumbackend.infra.exception;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonPropertyOrder({"statusCode", "name", "message", "action"})
public class ErrorResponse {

    private int statusCode;
    private String name;
    private String message;
    private String action;

    public ErrorResponse(BaseException exception) {
        this.statusCode = exception.getStatusCode();
        this.name = exception.getName();
        this.message = exception.getMessage();
        this.action = exception.getAction();
    }

}
