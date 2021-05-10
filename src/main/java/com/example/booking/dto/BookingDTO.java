package com.example.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDTO {

  private final Long id;
  private final String name;
  private final String email;
  private final LocalDate arrivalDate;
  private final LocalDate departureDate;
  private final String status;
}
