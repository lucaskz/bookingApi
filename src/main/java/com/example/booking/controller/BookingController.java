package com.example.booking.controller;

import com.example.booking.dto.BookingDTO;
import com.example.booking.facade.BookingFacade;
import com.example.booking.request.CreateBookingRequestBody;
import com.example.booking.request.UpdateBookingRequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

  private final BookingFacade bookingFacade;

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BookingDTO> newBooking(
      @RequestBody @Valid CreateBookingRequestBody createBookingRequestBody) {
    BookingDTO bookingDTO = this.bookingFacade.create(createBookingRequestBody);
    return ResponseEntity.status(HttpStatus.CREATED).body(bookingDTO);
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<BookingDTO> getBooking(@PathVariable @NotNull Long id) {
    BookingDTO bookingDTO = this.bookingFacade.find(id);
    return ResponseEntity.status(HttpStatus.OK).body(bookingDTO);
  }

  @DeleteMapping(value = "/{id}")
  public ResponseEntity<BookingDTO> cancelBooking(@PathVariable @NotNull Long id) {
    BookingDTO bookingDTO = this.bookingFacade.cancel(id);
    return ResponseEntity.status(HttpStatus.OK).body(bookingDTO);
  }

  @PatchMapping(value = "/{id}")
  public ResponseEntity<BookingDTO> updateBooking(
      @RequestBody @Valid UpdateBookingRequestBody updateBookingRequestBody,
      @PathVariable Long id) {
    BookingDTO bookingDTO = this.bookingFacade.update(id, updateBookingRequestBody);
    return ResponseEntity.status(HttpStatus.OK).body(bookingDTO);
  }
}
