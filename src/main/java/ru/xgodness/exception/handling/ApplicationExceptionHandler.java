package ru.xgodness.exception.handling;

import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log
@ControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    protected ResponseEntity<Object> handle(RuntimeException ex, WebRequest request) {
        log.warning("Caught UNHANDLED RuntimeException: [%s] %s".formatted(ex.getClass(), ex.getMessage()));
        log.warning(ex.getMessage());
        return super.handleExceptionInternal(ex,
                new ErrorMessages("Something went wrong"),
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }
}
