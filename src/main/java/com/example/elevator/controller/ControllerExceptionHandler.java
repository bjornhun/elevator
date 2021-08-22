package com.example.elevator.controller;

import com.example.elevator.exception.InvalidFloorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

  private static final String UNKNOWN_ERROR_MESSAGE = "Unknown error occured";

  @ExceptionHandler(InvalidFloorException.class)
  public ResponseEntity<String> handleInvalidFloorException(InvalidFloorException e) {
    log.warn(e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleUnknownExceptions(Exception e) {
    log.error(UNKNOWN_ERROR_MESSAGE, e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UNKNOWN_ERROR_MESSAGE);
  }
}