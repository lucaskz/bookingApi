package com.example.booking.service;

import com.example.booking.dto.BookingDTO;
import com.example.booking.exception.BookingNotFoundException;
import com.example.booking.exception.InvalidBookingStateException;
import com.example.booking.exception.UnavailableBookingDateException;
import com.example.booking.model.Booking;
import com.example.booking.model.BookingStatus;
import com.example.booking.repository.BookingRepository;
import com.example.booking.request.BookingRequestBody;
import com.example.booking.request.CreateBookingRequestBody;
import com.example.booking.request.UpdateBookingRequestBody;
import com.example.booking.transformer.BookingTransformer;
import com.vladmihalcea.hibernate.type.range.Range;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.example.booking.constant.ExceptionMessages.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

  private final BookingRepository bookingRepository;

  private final BookingTransformer bookingTransformer;

  @Transactional
  public BookingDTO find(Long id) {
    Booking booking = this.findBooking(id);
    return this.bookingTransformer.transform(booking);
  }

  /**
   * Creates a new Booking with status ACTIVE. Checks if arrival date and departure dates are
   * available. Thread-safe method, if multiple creation request are made-with overlapping dates-
   * only the first one is served.
   *
   * @param requestBody a valid request body
   * @return a booking id
   */
  @Transactional
  public Long create(CreateBookingRequestBody requestBody) {

    this.checkAvailableBookingDate(requestBody, null);

    Booking booking =
        new Booking(
            requestBody.getName(),
            requestBody.getEmail(),
            BookingStatus.ACTIVE,
            Range.closed(requestBody.getArrivalDate(), requestBody.getDepartureDate()));

    log.info(
        String.format(
            "Creating booking from %s to %s",
            booking.getBookingDateRange().lower(), booking.getBookingDateRange().upper()));

    booking = this.bookingRepository.save(booking);

    return booking.getId();
  }

  /**
   * Changes the status of a booking to CANCELLED. Also, frees the dates of arrival and departure
   * for future use. Thread-Safe method, if multiple cancel request are made, only the first one
   * will be served.
   *
   * @param bookingId to cancel.
   * @return a bookingDTO with updated values.
   */
  @Transactional
  public BookingDTO cancel(Long bookingId) {
    Booking booking = this.findBooking(bookingId);
    this.checkCancelledStatus(booking);

    LocalDate from = booking.getBookingDateRange().lower();
    LocalDate to = booking.getBookingDateRange().upper();

    booking.setStatus(BookingStatus.CANCELLED);
    booking.setBookingDateRange(null);

    log.info(String.format("Cancelling booking %s, from %s to %s", booking.getId(), from, to));

    booking = this.bookingRepository.save(booking);

    return this.bookingTransformer.transform(booking);
  }

  /**
   * When an update request is made, this method also checks availability on given dates.
   * Thread-safe method, if multiple request are made-for the same booking-, the first request will
   * be served, while the others will get an optimistic lock exception.
   *
   * @param id of the booking.
   * @param requestBody to update such booking.
   * @return a BookingDTO with updated values.
   */
  @Transactional
  public BookingDTO update(Long id, UpdateBookingRequestBody requestBody) {

    Booking booking = this.findBooking(id);
    this.checkCancelledStatus(booking);
    // check if email is present
    if (nonNull(requestBody.getEmail())) {
      booking.setEmail(requestBody.getEmail());
    }
    // check if user name is present
    if (nonNull(requestBody.getName())) {
      booking.setUserName((requestBody.getName()));
    }
    // if dates are present, i need to check if they are available
    if (nonNull(requestBody.getArrivalDate()) && nonNull(requestBody.getDepartureDate())) {
      this.checkAvailableBookingDate(requestBody, booking.getId());
      booking.setBookingDateRange(
          Range.closed(requestBody.getArrivalDate(), requestBody.getDepartureDate()));
    }

    log.info(String.format("Updating booking %s", booking.getId()));
    booking = this.bookingRepository.save(booking);

    return this.bookingTransformer.transform(booking);
  }

  private void checkAvailableBookingDate(BookingRequestBody requestBody, Long bookingId) {
    List<Booking> bookings =
        this.findBooking(requestBody.getArrivalDate(), requestBody.getDepartureDate());

    // the range of dates holds more than one booking already, i assume they are not available
    if (!CollectionUtils.isEmpty(bookings) && bookings.size() > 1) {
      throw new UnavailableBookingDateException(REQUESTED_DATE_NOT_AVAILABLE);
    }

    // if the booking is not mine, the date range is not available
    if (!CollectionUtils.isEmpty(bookings)) {
      Long id = bookings.stream().findFirst().map(Booking::getId).orElse(null);
      if (isNull(bookingId) || !bookingId.equals(id))
        throw new UnavailableBookingDateException(REQUESTED_DATE_NOT_AVAILABLE);
    }
  }

  private void checkCancelledStatus(Booking booking) {
    if (booking.isCancelled()) {
      throw new InvalidBookingStateException(INVALID_BOOKING_STATE);
    }
  }

  private List<Booking> findBooking(LocalDate arrivalDate, LocalDate departureDate) {
    return this.bookingRepository.findBookingDatesBetweenDateRange(arrivalDate, departureDate);
  }

  private Booking findBooking(Long id) {
    return this.bookingRepository
        .findById(id)
        .orElseThrow(() -> new BookingNotFoundException(BOOKING_NOT_FOUND));
  }
}
