package com.example.booking.service;

import com.example.booking.dto.AvailableDateDTO;
import com.example.booking.dto.AvailableDatesDTO;
import com.example.booking.request.CreateBookingRequestBody;
import db.DatabaseIT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:db/changelog/scripts/001-unique-date-range.sql")
public class AvailabilityServiceTest extends DatabaseIT {

  @Autowired private AvailabilityService availabilityService;

  @Autowired private BookingService bookingService;

  @Test
  public void whenBookingSuccess_DateShouldBeUnavailable() {
    LocalDate arrivalDate = LocalDate.now().plusDays(45);
    LocalDate departureDate = LocalDate.now().plusDays(46);
    CreateBookingRequestBody request =
        new CreateBookingRequestBody("test name", "test@mail.com", arrivalDate, departureDate);
    this.bookingService.create(request);

    AvailableDatesDTO availableDatesDTO =
        this.availabilityService.availabilityFor(
            LocalDate.now().plusDays(44), LocalDate.now().plusDays(48));

    Map<Integer, AvailableDateDTO> expectedAvailableDates = new HashMap<>();
    expectedAvailableDates.put(
        1,
        AvailableDateDTO.builder()
            .from(LocalDate.now().plusDays(44))
            .to(LocalDate.now().plusDays(44))
            .build());
    expectedAvailableDates.put(
        2,
        AvailableDateDTO.builder()
            .from(LocalDate.now().plusDays(47))
            .to(LocalDate.now().plusDays(48))
            .build());

    assertEquals(availableDatesDTO.getAvailableDates().size(), 2);

    int i = 1;
    for (AvailableDateDTO availableDate : availableDatesDTO.getAvailableDates()) {
      assertEquals(availableDate.getFrom(), expectedAvailableDates.get(i).getFrom());
      assertEquals(availableDate.getTo(), expectedAvailableDates.get(i).getTo());
      i++;
    }
  }
}
