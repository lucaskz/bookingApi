package com.example.booking.helper;

import com.vladmihalcea.hibernate.type.range.Range;

import java.time.LocalDate;

/** Converts any Open Bound (lower or upper) into -readable- closed bound. */
public class DateRangeHelper {

  public static LocalDate lower(Range<LocalDate> dateRange) {
    if (dateRange.hasLowerBound() && !dateRange.isLowerBoundClosed()) {
      return dateRange.lower().plusDays(1);
    }
    return dateRange.lower();
  }

  public static LocalDate upper(Range<LocalDate> dateRange) {
    if (dateRange.hasUpperBound() && !dateRange.isUpperBoundClosed()) {
      return dateRange.upper().minusDays(1);
    }
    return dateRange.upper();
  }
}
