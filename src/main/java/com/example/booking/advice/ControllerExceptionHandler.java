package com.example.booking.advice;

import com.example.booking.dto.ErrorResponseDTO;
import com.example.booking.exception.BookingNotFoundException;
import com.example.booking.exception.InvalidBookingStateException;
import com.example.booking.exception.UnavailableBookingDateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {UnavailableBookingDateException.class})
  protected ResponseEntity<Object> handleUnavailableBookingDateException(
      UnavailableBookingDateException ex, WebRequest request) {
    return this.handleExceptionInternal(
        ex, this.build(ex), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(value = {BookingNotFoundException.class})
  protected ResponseEntity<Object> handleBookingNotFoundException(
      BookingNotFoundException ex, WebRequest request) {
    return this.handleExceptionInternal(
        ex, this.build(ex), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler(value = {InvalidBookingStateException.class})
  protected ResponseEntity<Object> handleInvalidBookingStateException(
      InvalidBookingStateException ex, WebRequest request) {
    log.info("Invalid booking state exception", ex);
    return this.handleExceptionInternal(
        ex, this.build(ex), new HttpHeaders(), HttpStatus.CONFLICT, request);
  }

  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<Object> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {
    List<String> errors = new ArrayList<>();
    ex.getConstraintViolations().forEach(err -> errors.add(this.getErrorMsg(err)));

    return this.handleExceptionInternal(
        ex,
        this.build(ex.getLocalizedMessage(), errors),
        new HttpHeaders(),
        HttpStatus.BAD_REQUEST,
        request);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    log.info("Invalid request body", ex);
    return this.handleExceptionInternal(
        ex,
        this.build("Invalid request body", null),
        new HttpHeaders(),
        HttpStatus.BAD_REQUEST,
        request);
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  public ResponseEntity<Object> handleArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex, WebRequest request) {

    return this.handleExceptionInternal(
        ex,
        this.build(
            ex.getLocalizedMessage(), Collections.singletonList(this.getMissMatchErrorMsg(ex))),
        new HttpHeaders(),
        HttpStatus.BAD_REQUEST,
        request);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {

    List<String> details = new ArrayList<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            err -> details.add(String.format("%s : %s", err.getField(), err.getDefaultMessage())));
    ex.getBindingResult()
        .getGlobalErrors()
        .forEach(
            err ->
                details.add(
                    String.format("%s : %s", err.getObjectName(), err.getDefaultMessage())));

    return this.handleExceptionInternal(
        ex,
        this.build("Invalid argument exception", details),
        new HttpHeaders(),
        HttpStatus.BAD_REQUEST,
        request);
  }

  @ExceptionHandler({TimeoutException.class})
  public ResponseEntity<Object> handleTimeoutException(Exception ex, WebRequest request) {
    log.info(ex.getCause().getMessage(), ex);
    return this.handleExceptionInternal(
        ex, this.build(ex), new HttpHeaders(), HttpStatus.REQUEST_TIMEOUT, request);
  }

  @ExceptionHandler({Exception.class})
  public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
    log.error(ex.getCause().getMessage(), ex);
    return this.handleExceptionInternal(
        ex, this.build(ex), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  private ErrorResponseDTO build(Exception ex) {
    return ErrorResponseDTO.builder()
        .timestamp(new Date())
        .message(ex.getLocalizedMessage())
        .build();
  }

  private ErrorResponseDTO build(String message, List<String> details) {
    return ErrorResponseDTO.builder()
        .timestamp(new Date())
        .message(message)
        .details(details)
        .build();
  }

  private String getErrorMsg(ConstraintViolation<?> err) {
    return String.format(
        "%s %s : %s", err.getRootBeanClass().getName(), err.getPropertyPath(), err.getMessage());
  }

  private String getMissMatchErrorMsg(MethodArgumentTypeMismatchException ex) {
    return String.format("%s should be of type : %s", ex.getName(), ex.getRequiredType());
  }
}
