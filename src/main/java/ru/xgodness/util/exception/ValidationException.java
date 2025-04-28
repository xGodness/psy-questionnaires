package ru.xgodness.util.exception;

import ru.xgodness.exception.ApplicationException;
import ru.xgodness.exception.dto.ErrorMessages;

import java.util.List;

public class ValidationException extends ApplicationException {
    public ValidationException(ErrorMessages errorMessages) {
        super(errorMessages);
    }

    public ValidationException(String... errors) {
        super(errors);
    }

    public ValidationException(List<String> errors) {
        super(errors);
    }
}
