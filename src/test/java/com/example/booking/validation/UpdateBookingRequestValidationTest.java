package com.example.booking.validation;

import com.example.booking.request.UpdateBookingRequestBody;
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

public class UpdateBookingRequestValidationTest {

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
  public void whenEmptyDepartureDate_shouldBeInvalid() {
    UpdateBookingRequestBody requestBody =
        new UpdateBookingRequestBody(
            "test name", "mail@mail.com", LocalDate.now().plusDays(5), null);

    Set<ConstraintViolation<UpdateBookingRequestBody>> violations = validator.validate(requestBody);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void whenEmptyArrivalDate_shouldBeInvalid() {
    UpdateBookingRequestBody requestBody =
        new UpdateBookingRequestBody(
            "test name", "mail@mail.com", null, LocalDate.now().plusDays(5));

    Set<ConstraintViolation<UpdateBookingRequestBody>> violations = validator.validate(requestBody);

    assertFalse(violations.isEmpty());
  }

  @Test
  public void whenEmptyOrNullName_shouldBeValid() {
    UpdateBookingRequestBody requestBody =
        new UpdateBookingRequestBody(
            "", "mail@mail.com", LocalDate.now().plusDays(2), LocalDate.now().plusDays(3));

    Set<ConstraintViolation<UpdateBookingRequestBody>> violations = validator.validate(requestBody);

    assertTrue(violations.isEmpty());

    requestBody =
        new UpdateBookingRequestBody(
            null, "mail@mail.com", LocalDate.now().plusDays(2), LocalDate.now().plusDays(3));

    violations = validator.validate(requestBody);

    assertTrue(violations.isEmpty());
  }

  @Test
  public void whenEmptyOrNullMail_shouldBeValid() {
    UpdateBookingRequestBody requestBody =
        new UpdateBookingRequestBody(
            "test name", "", LocalDate.now().plusDays(2), LocalDate.now().plusDays(3));

    Set<ConstraintViolation<UpdateBookingRequestBody>> violations = validator.validate(requestBody);

    assertTrue(violations.isEmpty());

    requestBody =
        new UpdateBookingRequestBody(
            "test name", null, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3));

    violations = validator.validate(requestBody);

    assertTrue(violations.isEmpty());
  }
}
