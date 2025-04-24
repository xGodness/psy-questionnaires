package ru.xgodness.exception;

import ru.xgodness.exception.dto.ErrorMessages;

import java.util.List;

public class SQLExecutionException extends ApplicationException {
    public SQLExecutionException(ErrorMessages errorMessages) {
        super(errorMessages);
    }

    public SQLExecutionException(String... errors) {
        super(errors);
    }

    public SQLExecutionException(List<String> errors) {
        super(errors);
    }
}
