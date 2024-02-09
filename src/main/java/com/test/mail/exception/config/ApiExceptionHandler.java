package com.test.mail.exception.config;

import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ApiExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiExceptionResponse> handleExceptions(Exception e) {

    ApiExceptionResponse apiException = ApiExceptionResponse
        .builder().message(e.getMessage())
        .throwable(e)
        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .build();

    log.error(e.getMessage(), e);

    return new ResponseEntity<>(apiException, apiException.getHttpStatus());
  }

  @ExceptionHandler(value = { ApiException.class })
  public ResponseEntity<ApiExceptionResponse> handleApiException(ApiException e) {

    ApiExceptionResponse apiException = ApiExceptionResponse
        .builder().message(e.getMessage())
        .throwable(e)
        .httpStatus(HttpStatus.BAD_REQUEST)
        .build();

    log.error(e.getMessage());

    return new ResponseEntity<>(apiException, apiException.getHttpStatus());

  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiExceptionResponse> handleValidationExceptions(MethodArgumentNotValidException e) {

    String message = e.getBindingResult().getAllErrors().stream()
        .map(error -> {
          String fieldName = ((FieldError) error).getField();
          String errorMessage = error.getDefaultMessage();
          return fieldName + " : " + errorMessage;
        }).collect(Collectors.joining(";"));
    log.info(message);
    HttpStatus badRequest = HttpStatus.BAD_REQUEST;
    ApiExceptionResponse apiException = ApiExceptionResponse
        .builder().message(e.getMessage())
        .throwable(e)
        .httpStatus(badRequest)
        .build();

    log.error(e.getMessage());

    return new ResponseEntity<>(apiException, badRequest);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiExceptionResponse> handleValidationExceptions(ConstraintViolationException e) {

    ApiExceptionResponse apiException = ApiExceptionResponse
        .builder().message(e.getMessage())
        .throwable(e)
        .httpStatus(HttpStatus.BAD_REQUEST)
        .build();

    log.error(e.getMessage());

    return new ResponseEntity<>(apiException, apiException.getHttpStatus());

  }

}
