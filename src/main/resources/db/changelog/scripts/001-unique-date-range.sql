ALTER TABLE booking
    ADD CONSTRAINT unique_date_range
        EXCLUDE USING gist (
        booking_date_range WITH &&
        );