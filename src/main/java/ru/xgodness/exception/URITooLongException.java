package ru.xgodness.exception;

public class URITooLongException extends ApplicationException {
    public URITooLongException() {
        super("Request URI is too long");
    }
}
