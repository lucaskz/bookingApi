package com.example.booking.validation;

import com.example.booking.request.CreateBookingRequestBody;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NewBookingRequestValidationTest {

  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeClass
  public static void createValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterClass
  public static void close() {
    validatorFactory.close();
  }

  @Test
  public void whenRequestingSameDayAsArrival_shouldBeInvalid() {
    CreateBookingRequestBody requestBody =
        new CreateBookingRequestBody(
            "test name", "mail@mail.com", LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

    Set<ConstraintViolation<CreateBookingRequestBody>> violations = validator.validate(requestBody);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void whenRequestingPlusOneDayAsArrival_shouldBeValid() {
    CreateBookingRequestBody requestBody =
        new CreateBookingRequestBody(
            "test name", "mail@mail.com", LocalDate.now().plusDays(2), LocalDate.now().plusDays(3));

    Set<ConstraintViolation<CreateBookingRequestBody>> violations = validator.validate(requestBody);

    assertTrue(violations.isEmpty());
  }

  @Test
  public void whenRequestingPlusAsOneMonthAndOneDayArrival_shouldBeInValid() {
    CreateBookingRequestBody requestBody =
        new CreateBookingRequestBody(
            "test name",
            "mail@mail.com",
            LocalDate.now().plusMonths(1).plusDays(1),
            LocalDate.now().plusMonths(1).plusDays(2));

    Set<ConstraintViolation<CreateBookingRequestBody>> violations = validator.validate(requestBody);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void whenRequestingTooManyDays_shouldBeInValid() {
    CreateBookingRequestBody requestBody =
        new CreateBookingRequestBody(
            "test name", "mail@mail.com", LocalDate.now().plusDays(2), LocalDate.now().plusDays(7));

    Set<ConstraintViolation<CreateBookingRequestBody>> violations = validator.validate(requestBody);

    assertFalse(violations.isEmpty());
  }
}
