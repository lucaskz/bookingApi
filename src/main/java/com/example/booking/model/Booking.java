package com.example.booking.model;

import com.vladmihalcea.hibernate.type.range.PostgreSQLRangeType;
import com.vladmihalcea.hibernate.type.range.Range;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import java.time.LocalDate;

@Entity
@TypeDef(typeClass = PostgreSQLRangeType.class, defaultForType = Range.class)
@NoArgsConstructor
@Setter
@Getter
public class Booking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  private String userName;

  private String email;

  @Enumerated private BookingStatus status;

  @Column(name = "booking_date_range", columnDefinition = "daterange")
  private Range<LocalDate> bookingDateRange;

  public Booking(
      String userName, String email, BookingStatus status, Range<LocalDate> bookingDateRange) {
    this.userName = userName;
    this.email = email;
    this.status = status;
    this.bookingDateRange = bookingDateRange;
  }

  public boolean isCancelled() {
    return BookingStatus.CANCELLED.equals(this.status);
  }
}
