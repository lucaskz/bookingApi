package com.example.booking.request;

import java.time.LocalDate;

public interface BookingRequestBody {

  LocalDate getArrivalDate();

  LocalDate getDepartureDate();
}
