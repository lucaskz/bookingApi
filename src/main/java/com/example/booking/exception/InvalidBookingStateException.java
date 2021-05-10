package com.example.booking.exception;

public class InvalidBookingStateException extends RuntimeException {

  public InvalidBookingStateException(String s) {
    super(s);
  }

  public InvalidBookingStateException(String s, Throwable cause) {
    super(s, cause);
  }
}
