package com.example.booking.request;

import com.example.booking.annotation.ValidDates;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
@ValidDates
public class CreateBookingRequestBody implements BookingRequestBody {

  @NotBlank private final String name;

  @NotBlank @Email private final String email;

  @NotNull
  @Future
  @JsonFormat(pattern = "yyyy-MM-dd")
  private final LocalDate arrivalDate;

  @NotNull
  @Future
  @JsonFormat(pattern = "yyyy-MM-dd")
  private final LocalDate departureDate;
}
