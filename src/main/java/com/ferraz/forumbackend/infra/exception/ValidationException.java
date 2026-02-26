package com.ferraz.forumbackend.infra.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationException extends BaseException {

    public ValidationException(InvalidField invalidField) {
        this(List.of(invalidField));
    }

    public ValidationException(List<InvalidField> invalidFields) {
        super(
                "Corpo da requisição inválido",
                HttpStatus.BAD_REQUEST.value(),
                ValidationException.class.getSimpleName(),
                "Revise os erros dos campos inválidos",
                invalidFields
        );
    }
}
