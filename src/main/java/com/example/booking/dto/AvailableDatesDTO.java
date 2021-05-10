package com.example.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailableDatesDTO {

  private final Collection<AvailableDateDTO> availableDates;
}
