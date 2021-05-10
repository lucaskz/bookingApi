package com.example.booking.model;

import lombok.Getter;

@Getter
public enum BookingStatus {
  ACTIVE(1),
  CANCELLED(0);

  Integer id;

  BookingStatus(int id) {
    this.id = id;
  }
}
