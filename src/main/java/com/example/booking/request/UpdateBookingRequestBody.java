package com.example.booking.request;

import com.example.booking.annotation.ValidDates;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
@ValidDates
public class UpdateBookingRequestBody implements BookingRequestBody {

  private final String name;

  @Email private final String email;

  @Future
  @JsonFormat(pattern = "yyyy-MM-dd")
  private final LocalDate arrivalDate;

  @Future
  @JsonFormat(pattern = "yyyy-MM-dd")
  private final LocalDate departureDate;
}
