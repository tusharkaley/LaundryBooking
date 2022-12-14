package com.laundrybooking.accessor;

import com.laundrybooking.model.Booking;
import com.laundrybooking.model.BookingStatus;
import com.laundrybooking.model.House;

public class BookingDataAccessor implements DataAccessor<Booking> {

    @Override public void create(final Booking booking) {
        // No Op data accessor
    }

    @Override public Booking read(final String id) {
        return null;
    }

    public Booking read(final House house, final BookingStatus bookingStatus) {
        return null;
    }

    @Override public void update() {

    }

    @Override public void delete() {

    }
}
