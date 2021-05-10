package com.example.booking.repository;

import com.example.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

  @Query(
      value =
          "select *  from Booking b where b.booking_date_range && daterange(''||'[' || :d1 ||',' || :d2 ||']' || '')",
      nativeQuery = true)
  List<Booking> findBookingDatesBetweenDateRange(
      @Param("d1") LocalDate d1, @Param("d2") LocalDate d2);
}
