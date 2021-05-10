package com.example.booking.service;

import com.example.booking.dto.BookingDTO;
import com.example.booking.exception.InvalidBookingStateException;
import com.example.booking.exception.UnavailableBookingDateException;
import com.example.booking.request.CreateBookingRequestBody;
import com.example.booking.request.UpdateBookingRequestBody;
import db.DatabaseIT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:db/changelog/scripts/001-unique-date-range.sql")
public class BookingServiceTest extends DatabaseIT {

  @Autowired private BookingService bookingService;

  @Test
  public void whenCreatingBooking_thoseDatesShouldBeUnavailable() {
    LocalDate arrivalDate = LocalDate.now().plusDays(2);
    LocalDate departureDate = LocalDate.now().plusDays(3);
    CreateBookingRequestBody request =
        new CreateBookingRequestBody("test name", "test@mail.com", arrivalDate, departureDate);
    this.bookingService.create(request);
    // try to create again
    assertThrows(UnavailableBookingDateException.class, () -> this.bookingService.create(request));
  }

  @Test
  public void whenCancellingCancelledBooking_shouldFail() {
    LocalDate arrivalDate = LocalDate.now().plusDays(4);
    LocalDate departureDate = LocalDate.now().plusDays(5);
    CreateBookingRequestBody request =
        new CreateBookingRequestBody("test name", "test@mail.com", arrivalDate, departureDate);
    Long id = this.bookingService.create(request);

    this.bookingService.cancel(id);
    assertThrows(InvalidBookingStateException.class, () -> this.bookingService.cancel(id));
  }

  @Test
  public void whenUpdatingCancelledBooking_shouldFail() {
    LocalDate arrivalDate = LocalDate.now().plusDays(16);
    LocalDate departureDate = LocalDate.now().plusDays(18);
    CreateBookingRequestBody request =
        new CreateBookingRequestBody("test name", "test@mail.com", arrivalDate, departureDate);
    Long id = this.bookingService.create(request);

    this.bookingService.cancel(id);

    UpdateBookingRequestBody updateRequest =
        new UpdateBookingRequestBody("test name updated", null, arrivalDate, departureDate);

    assertThrows(
        InvalidBookingStateException.class, () -> this.bookingService.update(id, updateRequest));
  }

  @Test
  public void whenOnlyUpdatingEmail_onlyEmailShouldBeUpdated() {
    LocalDate arrivalDate = LocalDate.now().plusDays(21);
    LocalDate departureDate = LocalDate.now().plusDays(22);
    String originalName = "test name";
    String updatedEmail = "updated@mail.com";
    CreateBookingRequestBody request =
        new CreateBookingRequestBody(originalName, "test@mail.com", arrivalDate, departureDate);
    Long id = this.bookingService.create(request);

    UpdateBookingRequestBody updateRequest =
        new UpdateBookingRequestBody(null, updatedEmail, null, null);

    BookingDTO bookingDTO = this.bookingService.update(id, updateRequest);

    assertAll(
        () -> assertEquals(bookingDTO.getName(), originalName),
        () -> assertEquals(bookingDTO.getArrivalDate(), arrivalDate),
        () -> assertEquals(bookingDTO.getDepartureDate(), departureDate),
        () -> assertEquals(bookingDTO.getEmail(), updatedEmail));
  }

  @Test
  public void whenOnlyUpdatingUserName_onlyUserMailShouldBeUpdated() {
    LocalDate arrivalDate = LocalDate.now().plusDays(6);
    LocalDate departureDate = LocalDate.now().plusDays(7);
    String updateUserName = "updated user name";
    String originalEmail = "mail@mail.com";
    CreateBookingRequestBody request =
        new CreateBookingRequestBody("test name", originalEmail, arrivalDate, departureDate);
    Long id = this.bookingService.create(request);

    UpdateBookingRequestBody updateRequest =
        new UpdateBookingRequestBody(updateUserName, null, null, null);

    BookingDTO bookingDTO = this.bookingService.update(id, updateRequest);

    assertAll(
        () -> assertEquals(bookingDTO.getName(), updateUserName),
        () -> assertEquals(bookingDTO.getArrivalDate(), arrivalDate),
        () -> assertEquals(bookingDTO.getDepartureDate(), departureDate),
        () -> assertEquals(bookingDTO.getEmail(), originalEmail));
  }

  /**
   * Creates two bookings with different days. Then, one is trying to update to an -already- used
   * Date, which is not from own his booking.
   */
  @Test
  public void whenUpdatingBookingToUnavailableDate_shouldFail() {
    LocalDate unAvailableArrivalDate = LocalDate.now().plusDays(35);
    LocalDate unAvailableDepartureDate = LocalDate.now().plusDays(36);
    CreateBookingRequestBody request =
        new CreateBookingRequestBody(
            "test name", "test@mail.com", unAvailableArrivalDate, unAvailableDepartureDate);
    this.bookingService.create(request);

    CreateBookingRequestBody secondRequest =
        new CreateBookingRequestBody(
            "other name",
            "other@mail.com",
            LocalDate.now().plusDays(37),
            LocalDate.now().plusDays(38));

    Long id = this.bookingService.create(secondRequest);

    UpdateBookingRequestBody updateRequest =
        new UpdateBookingRequestBody(
            null, null, LocalDate.now().plusDays(36), LocalDate.now().plusDays(38));

    assertThrows(
        UnavailableBookingDateException.class, () -> this.bookingService.update(id, updateRequest));
  }

  /** Updating a booking should check if the date range is from his own reservation. */
  @Test
  public void whenUpdatingMyOwnBookingDate_shouldSuccess() {
    LocalDate unAvailableArrivalDate = LocalDate.now().plusDays(12);
    LocalDate unAvailableDepartureDate = LocalDate.now().plusDays(13);
    CreateBookingRequestBody request =
        new CreateBookingRequestBody(
            "test name", "test@mail.com", unAvailableArrivalDate, unAvailableDepartureDate);
    Long id = this.bookingService.create(request);

    UpdateBookingRequestBody updateRequest =
        new UpdateBookingRequestBody(
            null, null, LocalDate.now().plusDays(13), LocalDate.now().plusDays(14));

    BookingDTO bookingDTO = this.bookingService.update(id, updateRequest);

    assertAll(
        () -> assertEquals(bookingDTO.getArrivalDate(), LocalDate.now().plusDays(13)),
        () -> assertEquals(bookingDTO.getDepartureDate(), LocalDate.now().plusDays(14)));
  }

  @Test
  public void whenMultipleBookingRequest_shouldCreateOneAndRejectOthers()
      throws InterruptedException {

    final ExecutorService executor = Executors.newFixedThreadPool(3);

    ConcurrentHashMap<Integer, Long> responses = new ConcurrentHashMap<>();

    ConcurrentHashMap<Integer, Exception> exceptions = new ConcurrentHashMap<>();

    executor.execute(
        () ->
            createBooking(
                1,
                LocalDate.now().plusDays(23),
                LocalDate.now().plusDays(24),
                responses,
                exceptions));

    executor.execute(
        () ->
            createBooking(
                2,
                LocalDate.now().plusDays(23),
                LocalDate.now().plusDays(24),
                responses,
                exceptions));

    executor.execute(
        () ->
            createBooking(
                3,
                LocalDate.now().plusDays(23),
                LocalDate.now().plusDays(24),
                responses,
                exceptions));

    executor.shutdown();

    executor.awaitTermination(10, TimeUnit.SECONDS);

    assertAll(
        () -> assertEquals(responses.values().size(), 1),
        () -> assertEquals(exceptions.values().size(), 2));
  }

  private void createBooking(
      int i,
      LocalDate from,
      LocalDate to,
      ConcurrentHashMap<Integer, Long> responses,
      ConcurrentHashMap<Integer, Exception> exceptions) {

    try {
      Long id =
          this.bookingService.create(
              new CreateBookingRequestBody(String.valueOf(i), "test@mail.com", from, to));
      responses.put(i, id);
    } catch (DataIntegrityViolationException ex) {
      exceptions.put(i, ex);
    }
  }
}
