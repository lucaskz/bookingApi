package com.example.booking.controller;

import com.example.booking.dto.BookingDTO;
import com.example.booking.request.BookingRequestBody;
import com.example.booking.request.CreateBookingRequestBody;
import com.example.booking.request.UpdateBookingRequestBody;
import com.example.booking.service.BookingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import db.DatabaseIT;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:db/changelog/scripts/001-unique-date-range.sql")
public class BookingApplicationTest extends DatabaseIT {

  private MockMvc mvc;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private BookingService bookingService;

  @Before()
  public void setUp() {
    this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Test
  public void whenValidCreateBookingRequest_shouldSuccessWithStatusCreated() throws Exception {
    CreateBookingRequestBody createBookingRequestBody =
        new CreateBookingRequestBody(
            "test_name",
            "good.mail@mail.com",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(11));
    MockHttpServletResponse httpResponse = this.postCall("/booking", createBookingRequestBody);

    assertEquals(HttpStatus.CREATED.value(), httpResponse.getStatus());
  }

  @Test
  public void whenInvalidMail_shouldFailWithStatusBadRequest() throws Exception {
    CreateBookingRequestBody createBookingRequestBody =
        new CreateBookingRequestBody(
            "test_name", "bad_mail.com", LocalDate.now(), LocalDate.now().plusDays(1));
    MockHttpServletResponse httpResponse = this.postCall("/booking", createBookingRequestBody);

    assertEquals(HttpStatus.BAD_REQUEST.value(), httpResponse.getStatus());
  }

  @Test
  public void whenEmptyUserName_shouldFailWithStatusBadRequest() throws Exception {
    CreateBookingRequestBody createBookingRequestBody =
        new CreateBookingRequestBody(
            "", "mail@mail.com", LocalDate.now(), LocalDate.now().plusDays(1));
    MockHttpServletResponse httpResponse = this.postCall("/booking", createBookingRequestBody);

    assertEquals(HttpStatus.BAD_REQUEST.value(), httpResponse.getStatus());
  }

  /**
   * Multiple request trying to update the same Booking. Only one of them should get the status OK.
   * The other two should have a response with status CONFLICT.
   *
   * @throws InterruptedException -.
   */
  @Test
  public void whenMultipleUpdateNameRequest_shouldAcceptOneAndRejectOthers()
      throws InterruptedException {
    final Long id =
        this.bookingService.create(
            new CreateBookingRequestBody(
                "testName",
                "testMail@mail.com",
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(15)));

    final ExecutorService executor = Executors.newFixedThreadPool(5);

    ConcurrentHashMap<Integer, MockHttpServletResponse> responseMap = new ConcurrentHashMap<>();

    executor.execute(() -> updateBooking(id, 1, responseMap));

    executor.execute(() -> updateBooking(id, 2, responseMap));

    executor.execute(() -> updateBooking(id, 3, responseMap));

    executor.shutdown();

    executor.awaitTermination(10, TimeUnit.SECONDS);

    final BookingDTO updatedBooking = this.bookingService.find(id);

    Optional<Integer> updatedResponse =
        responseMap.entrySet().stream()
            .filter(entry -> HttpStatus.OK.value() == entry.getValue().getStatus())
            .map(Map.Entry::getKey)
            .findFirst();

    assertAll(
        () ->
            assertEquals(
                updatedBooking.getName(), updatedResponse.map(String::valueOf).orElse(null)),
        () ->
            assertEquals(
                2,
                responseMap.values().stream()
                    .filter(response -> HttpStatus.CONFLICT.value() == response.getStatus())
                    .count()));
  }

  private void updateBooking(
      Long id, int number, ConcurrentHashMap<Integer, MockHttpServletResponse> responseMap) {
    UpdateBookingRequestBody updateBookingRequestBody =
        new UpdateBookingRequestBody(String.format("%d", number), null, null, null);
    try {
      MockHttpServletResponse httpResponse =
          this.patchCall(String.format("/booking/%s", id), updateBookingRequestBody);
      responseMap.putIfAbsent(number, httpResponse);
    } catch (Exception ignored) {
    }
  }

  private MockHttpServletResponse postCall(String uri, BookingRequestBody body) throws Exception {
    return mvc.perform(
            post(uri).contentType(MediaType.APPLICATION_JSON_VALUE).content(this.mapToJson(body)))
        .andReturn()
        .getResponse();
  }

  private MockHttpServletResponse patchCall(String uri, BookingRequestBody body) throws Exception {
    return mvc.perform(
            patch(uri).contentType(MediaType.APPLICATION_JSON_VALUE).content(this.mapToJson(body)))
        .andReturn()
        .getResponse();
  }

  private MockHttpServletResponse getCall(String uri) throws Exception {
    return mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse();
  }

  private String mapToJson(Object body) throws JsonProcessingException {
    return objectMapper.writeValueAsString(body);
  }
}
