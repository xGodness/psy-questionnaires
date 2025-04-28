package ru.xgodness.security.exception;

import ru.xgodness.exception.ApplicationException;

public class URITooLongException extends ApplicationException {
    public URITooLongException() {
        super("Request URI is too long");
    }
}
