package net.kleinschmager.bowling.controller;

import net.kleinschmager.bowling.api.model.Error;
import net.kleinschmager.bowling.service.BadRequestException;
import net.kleinschmager.bowling.service.ConflictException;
import net.kleinschmager.bowling.service.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.OffsetDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ResponseEntity<Error> handleBadRequest(BadRequestException ex) {
        Error err = new Error();
        err.setError("BAD_REQUEST");
        err.setMessage(ex.getMessage());
        err.setTimestamp(OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ResponseEntity<Error> handleNotFound(NotFoundException ex) {
        Error err = new Error("NOT_FOUND", ex.getMessage(), OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseBody
    public ResponseEntity<Error> handleConflict(ConflictException ex) {
        Error err = new Error("CONFLICT", ex.getMessage(), OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Error> handleGeneric(Exception ex) {
        Error err = new Error("SERVER_ERROR", ex.getMessage() == null ? "Internal server error" : ex.getMessage(), OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
