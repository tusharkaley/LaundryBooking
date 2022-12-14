package com.laundrybooking.handlers;

import com.laundrybooking.accessor.BookingDataAccessor;
import com.laundrybooking.builder.ResponseBuilder;
import com.laundrybooking.model.Booking;
import com.laundrybooking.model.Response;

import java.util.Collections;
import java.util.List;

public class BookingHandler {

    ResponseBuilder responseBuilder;

    BookingDataAccessor bookingDataAccessor;

    public BookingHandler(final BookingDataAccessor bookingDataAccessor) {
        responseBuilder  = new ResponseBuilder();
        this.bookingDataAccessor = bookingDataAccessor;
    }

    public Response book(final int laundryRoomId, final int houseId, final String bookingStartTimeUTC, final String bookingEndTimeUTC) {
        // Cap on number of bookings per week
            // canBook function which checks if the user is allowed to book a laundry slot
            //
        return responseBuilder.buildSuccessResponse("");
    }

    public List<Booking> listBookedTimes() {
        // Show booked times for the next 1 month
        return Collections.emptyList();
    }

    public Response cancelBooking(final String bookingId) {
        return responseBuilder.buildSuccessResponse("");
    }
}
