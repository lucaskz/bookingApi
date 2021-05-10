package com.example.booking.transformer;

import com.example.booking.dto.BookingDTO;
import com.example.booking.helper.DateRangeHelper;
import com.example.booking.model.Booking;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class BookingTransformer {

  @Transactional(propagation = Propagation.MANDATORY)
  public BookingDTO transform(Booking booking) {
    return BookingDTO.builder()
        .id(booking.getId())
        .name(booking.getUserName())
        .email(booking.getEmail())
        .arrivalDate(
            Optional.ofNullable(booking.getBookingDateRange())
                .map(DateRangeHelper::lower)
                .orElse(null))
        .departureDate(
            Optional.ofNullable(booking.getBookingDateRange())
                .map(DateRangeHelper::upper)
                .orElse(null))
        .status(Optional.ofNullable(booking.getStatus()).map(Enum::name).orElse(null))
        .build();
  }
}
