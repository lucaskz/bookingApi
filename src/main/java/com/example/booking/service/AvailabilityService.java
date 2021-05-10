package com.example.booking.service;

import com.example.booking.dto.AvailableDatesDTO;
import com.example.booking.helper.DateRangeHelper;
import com.example.booking.model.Booking;
import com.example.booking.repository.BookingRepository;
import com.example.booking.transformer.AvailableDateRangeTransformer;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

  private final BookingRepository bookingRepository;

  private final AvailableDateRangeTransformer availableDateRangeTransformer;

  @Transactional
  public AvailableDatesDTO availabilityFor(LocalDate from, LocalDate to) {
    List<Booking> booked = this.bookingRepository.findBookingDatesBetweenDateRange(from, to);
    RangeSet<LocalDate> rangeSet = TreeRangeSet.create();
    for (Booking booking : booked) {
      rangeSet.add(
          Range.closed(
              DateRangeHelper.lower(booking.getBookingDateRange()),
              DateRangeHelper.upper(booking.getBookingDateRange())));
    }
    return this.availableDateRangeTransformer.transform(
        rangeSet.complement().subRangeSet(Range.closed(from, to)));
  }
}
