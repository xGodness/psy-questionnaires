package ru.xgodness.exception;

public class SQLExecutionException extends ApplicationException {
    public SQLExecutionException(String message) {
        super(message);
    }

    public SQLExecutionException() {
    }
}
