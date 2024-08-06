package com.hepsiemlak.todo.controller;

import com.hepsiemlak.todo.exception.ConflictException;
import com.hepsiemlak.todo.exception.NotFoundException;
import com.hepsiemlak.todo.exception.UnauthorizedException;
import com.hepsiemlak.todo.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        List<ErrorResponse.Error> errors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(objectError -> errors.add(ErrorResponse.Error.builder()
                .message(objectError.getField() + " " + objectError.getDefaultMessage()).build()));

        return new ResponseEntity<>(ErrorResponse.builder().errors(errors).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(NotFoundException ex) {
        List<ErrorResponse.Error> errors = new ArrayList<>();
        errors.add(ErrorResponse.Error.builder().message(ex.getMessage()).build());
        return new ResponseEntity<>(ErrorResponse.builder().errors(errors).build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
        List<ErrorResponse.Error> errors = new ArrayList<>();
        errors.add(ErrorResponse.Error.builder().message(ex.getMessage()).build());
        return new ResponseEntity<>(ErrorResponse.builder().errors(errors).build(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        List<ErrorResponse.Error> errors = new ArrayList<>();
        errors.add(ErrorResponse.Error.builder().message(ex.getMessage()).build());
        return new ResponseEntity<>(ErrorResponse.builder().errors(errors).build(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        List<ErrorResponse.Error> errors = new ArrayList<>();
        errors.add(ErrorResponse.Error.builder().message(ex.getMessage()).build());
        return new ResponseEntity<>(ErrorResponse.builder().errors(errors).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}