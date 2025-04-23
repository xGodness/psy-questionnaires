package ru.xgodness.exception;

import lombok.Getter;
import ru.xgodness.exception.handling.ErrorMessages;

import java.util.List;

@Getter
public class ApplicationException extends RuntimeException {
    private final ErrorMessages errorMessages;

    public ApplicationException(ErrorMessages errorMessages) {
        this.errorMessages = errorMessages;
    }

    public ApplicationException(String... errors) {
        this.errorMessages = new ErrorMessages(errors);
    }

    public ApplicationException(List<String> errors) {
        this.errorMessages = new ErrorMessages(errors);
    }
}

