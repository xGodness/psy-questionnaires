package ru.xgodness.exception;

import ru.xgodness.exception.handling.ErrorMessages;

import java.util.List;

public class AuthException extends ApplicationException {
    public AuthException(ErrorMessages errorMessages) {
        super(errorMessages);
    }

    public AuthException(String... errors) {
        super(errors);
    }

    public AuthException(List<String> errors) {
        super(errors);
    }
}
