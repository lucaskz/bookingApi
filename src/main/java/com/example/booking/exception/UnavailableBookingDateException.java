package com.example.booking.exception;

public class UnavailableBookingDateException extends RuntimeException {

  public UnavailableBookingDateException(String s) {
    super(s);
  }
}
