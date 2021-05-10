package com.example.booking.controller;

import com.example.booking.dto.AvailableDatesDTO;
import com.example.booking.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Future;
import java.time.LocalDate;

@RestController
@Validated
@RequestMapping("/availability")
@RequiredArgsConstructor
public class AvailabilityController {

  private final AvailabilityService availabilityService;

  @GetMapping()
  public ResponseEntity<AvailableDatesDTO> getAvailability(
      @RequestParam(value = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Valid @Future
          LocalDate from,
      @RequestParam(value = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Valid @Future
          LocalDate to) {
    AvailableDatesDTO availableDates = this.availabilityService.availabilityFor(from, to);
    return ResponseEntity.status(HttpStatus.OK).body(availableDates);
  }
}
