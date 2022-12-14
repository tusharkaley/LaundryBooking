package com.laundrybooking.accessor;

import com.laundrybooking.model.Booking;
import com.laundrybooking.model.BookingStatus;
import com.laundrybooking.model.House;
import com.laundrybooking.model.LaundryRoom;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * NO OP class for accessing the booking data
 */

public class BookingDataAccessor implements DataAccessor<Booking> {

    @Override public void create(final Booking booking) {
        // No Op data accessor
    }

    @Override public Booking read(final String id) {
        // No Op data accessor
        return Booking.builder().build();
    }

    public Booking read(final House house, final BookingStatus bookingStatus) {
        // No Op data accessor
        return Booking.builder().build();
    }

    public Booking read(final String id, final String houseId, final BookingStatus bookingStatus) {
        // No Op data accessor
        return Booking.builder().build();
    }

    public Booking read(final LaundryRoom laundryRoom, final String bookingStartDateTimeUTC,
                        final String bookingEndDateTimeUTC, final BookingStatus bookingStatus) {
        // No Op data accessor
        return Booking.builder().build();
    }

    public List<Booking> read(final Instant startTime, final Instant endTime, final BookingStatus bookingStatus) {
        // No Op data accessor
        return Collections.emptyList();
    }

    @Override public void update() {

    }

    public void update(final String id, final String fieldName, final String fieldValue) {

    }

    @Override public void delete() {

    }
}
