package com.example.booking.exception;

public class BookingNotFoundException extends RuntimeException {

  public BookingNotFoundException(String s) {
    super(s);
  }
}
