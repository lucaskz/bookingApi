package com.example.booking.validation;

import com.example.booking.annotation.ValidDates;
import com.example.booking.request.BookingRequestBody;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Objects.isNull;

public class DateValidator implements ConstraintValidator<ValidDates, BookingRequestBody> {

  private static final int MAX_ANTICIPATION_MONTHS = 1;
  private static final int MAX_NUMBER_OF_DAYS = 3;
  private static final int MIN_AHEAD_ARRIVAL = 1;

  @Override
  public boolean isValid(BookingRequestBody requestBody, ConstraintValidatorContext context) {
    if (isNull(requestBody.getArrivalDate()) && isNull(requestBody.getDepartureDate())) {
      return true;
    }

    if (isNull(requestBody.getArrivalDate())) {
      return buildConstraintViolation(context, "Missing arrival date", "arrivalDate");
    }

    if (isNull(requestBody.getDepartureDate())) {
      return buildConstraintViolation(context, "Missing departure date", "departureDate");
    }

    if (requestBody.getArrivalDate().isAfter(requestBody.getDepartureDate())
        || requestBody.getArrivalDate().isEqual(requestBody.getDepartureDate())) {
      return buildConstraintViolation(
          context, "Departure date must be higher than arrival date", "departureDate");
    }

    if (DAYS.between(requestBody.getArrivalDate(), requestBody.getDepartureDate())
        > MAX_NUMBER_OF_DAYS) {
      return buildConstraintViolation(
          context,
          String.format("Can't book more than %s day(s)", MAX_NUMBER_OF_DAYS),
          "departureDate");
    }

    if (LocalDate.now().plusMonths(MAX_ANTICIPATION_MONTHS).compareTo(requestBody.getArrivalDate())
        < 0) {
      return buildConstraintViolation(
          context,
          String.format(
              "Arrival date must be less than %s",
              LocalDate.now().plusMonths(MAX_ANTICIPATION_MONTHS)),
          "arrivalDate");
    }

    if (DAYS.between(LocalDate.now().plusDays(MIN_AHEAD_ARRIVAL), requestBody.getArrivalDate())
        < MIN_AHEAD_ARRIVAL) {
      return buildConstraintViolation(
          context,
          String.format("Must have at last %s day(s) before arrival", MIN_AHEAD_ARRIVAL),
          "arrivalDate");
    }

    return true;
  }

  private boolean buildConstraintViolation(
      ConstraintValidatorContext context, String message, String propertyNode) {
    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(message)
        .addPropertyNode(propertyNode)
        .addConstraintViolation();
    return false;
  }
}
