package com.example.booking.facade;

import com.example.booking.dto.BookingDTO;
import com.example.booking.exception.InvalidBookingStateException;
import com.example.booking.request.CreateBookingRequestBody;
import com.example.booking.request.UpdateBookingRequestBody;
import com.example.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import static com.example.booking.constant.ExceptionMessages.BOOKING_BEING_MODIFIED;
import static com.example.booking.constant.ExceptionMessages.REQUESTED_BOOKING_CANNOT_BE_CREATED;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingFacade {

  private final BookingService bookingService;

  public BookingDTO create(CreateBookingRequestBody createBookingRequestBody) {
    Long id;
    try {
      id = this.bookingService.create(createBookingRequestBody);
    } catch (DataIntegrityViolationException ex) {
      throw new InvalidBookingStateException(REQUESTED_BOOKING_CANNOT_BE_CREATED, ex);
    }

    return BookingDTO.builder().id(id).build();
  }

  public BookingDTO find(Long id) {
    return this.bookingService.find(id);
  }

  public BookingDTO cancel(Long id) {
    BookingDTO booking;
    try {
      booking = this.bookingService.cancel(id);
    } catch (OptimisticLockingFailureException ex) {
      throw new InvalidBookingStateException(BOOKING_BEING_MODIFIED);
    }
    return booking;
  }

  public BookingDTO update(Long id, UpdateBookingRequestBody updateBookingRequestBody) {
    BookingDTO booking;
    try {
      booking = this.bookingService.update(id, updateBookingRequestBody);
    } catch (OptimisticLockingFailureException ex) {
      throw new InvalidBookingStateException(BOOKING_BEING_MODIFIED);
    }
    return booking;
  }
}
