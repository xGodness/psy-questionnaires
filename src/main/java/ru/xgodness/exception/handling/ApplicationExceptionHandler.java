package ru.xgodness.exception.handling;

import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.xgodness.exception.*;

@Log
@ControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    protected ResponseEntity<Object> handle(RuntimeException ex, WebRequest request) {
        log.severe("Caught UNHANDLED RuntimeException: [%s] %s".formatted(ex.getClass(), ex.getMessage()));
        log.severe(ex.getMessage());
        return super.handleExceptionInternal(ex,
                new ErrorMessages("Something went wrong"),
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    @ExceptionHandler(value = SQLExecutionException.class)
    protected ResponseEntity<Object> handle(SQLExecutionException ex, WebRequest request) {
        log.severe("Caught SQLExecutionException: [%s] %s".formatted(ex.getClass(), ex.getMessage()));
        log.severe(ex.getMessage());
        return super.handleExceptionInternal(ex,
                new ErrorMessages("Something went wrong"),
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    @ExceptionHandler(value = AuthException.class)
    protected ResponseEntity<Object> handle(AuthException ex, WebRequest request) {
        log.info("Caught AuthException: " + ex.getMessage());
        return super.handleExceptionInternal(
                ex,
                ex.getErrorMessages(),
                new HttpHeaders(),
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    @ExceptionHandler(value = ValidationException.class)
    protected ResponseEntity<Object> handle(ValidationException ex, WebRequest request) {
        log.info("Caught ValidationException: " + ex.getMessage());
        return super.handleExceptionInternal(
                ex,
                ex.getErrorMessages(),
                new HttpHeaders(),
                HttpStatus.UNPROCESSABLE_ENTITY,
                request
        );
    }

    @ExceptionHandler(value = UsernameAlreadyTakenException.class)
    protected ResponseEntity<Object> handle(UsernameAlreadyTakenException ex, WebRequest request) {
        log.info("Caught UsernameAlreadyTakenException: " + ex.getMessage());
        return super.handleExceptionInternal(
                ex,
                ex.getErrorMessages(),
                new HttpHeaders(),
                HttpStatus.CONFLICT,
                request
        );
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    protected ResponseEntity<Object> handle(UsernameNotFoundException ex, WebRequest request) {
        log.info("Caught UsernameNotFoundException: " + ex.getMessage());
        return super.handleExceptionInternal(
                ex,
                ex.getErrorMessages(),
                new HttpHeaders(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Caught HttpRequestMethodNotSupportedException: " + ex.getMessage());
        return super.handleExceptionInternal(
                ex,
                new ErrorMessages("Http method is not allowed"),
                new HttpHeaders(),
                HttpStatus.METHOD_NOT_ALLOWED,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Caught HttpMessageNotReadableException: " + ex.getMessage());
        return super.handleExceptionInternal(
                ex,
                new ErrorMessages("Request body is not readable"),
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Caught NoResourceFoundException: " + ex.getMessage());
        return super.handleExceptionInternal(
                ex,
                new ErrorMessages("Resource does not exist"),
                new HttpHeaders(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Caught HttpMediaTypeNotSupportedException: " + ex.getMessage());
        return super.handleExceptionInternal(
                ex,
                new ErrorMessages("Media type is not supported"),
                new HttpHeaders(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                request
        );
    }
}
