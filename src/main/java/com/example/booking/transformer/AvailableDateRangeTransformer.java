package com.example.booking.transformer;

import com.example.booking.dto.AvailableDateDTO;
import com.example.booking.dto.AvailableDatesDTO;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class AvailableDateRangeTransformer {

  @Transactional(propagation = Propagation.MANDATORY)
  public AvailableDatesDTO transform(RangeSet<LocalDate> availableDates) {
    Collection<AvailableDateDTO> response = new ArrayList<>();
    availableDates
        .asRanges()
        .forEach(
            localDateRange ->
                response.add(
                    AvailableDateDTO.builder()
                        .from(getLowerBound(localDateRange))
                        .to(getUpperBound(localDateRange))
                        .build()));
    return AvailableDatesDTO.builder().availableDates(response).build();
  }

  private LocalDate getLowerBound(Range<LocalDate> localDateRange) {
    return BoundType.CLOSED.equals(localDateRange.lowerBoundType())
        ? localDateRange.lowerEndpoint()
        : localDateRange.lowerEndpoint().plusDays(1);
  }

  private LocalDate getUpperBound(Range<LocalDate> localDateRange) {
    return BoundType.CLOSED.equals(localDateRange.upperBoundType())
        ? localDateRange.upperEndpoint()
        : localDateRange.upperEndpoint().minusDays(1);
  }
}
