package com.laundrybooking.handlers;

import static com.laundrybooking.utils.Constants.HTTP_500_MESSAGE;
import static com.laundrybooking.utils.JsonUtils.buildJsonString;

import com.laundrybooking.accessor.BookingDataAccessor;
import com.laundrybooking.accessor.HouseDataAccessor;
import com.laundrybooking.accessor.LaundryRoomDataAccessor;
import com.laundrybooking.builder.ResponseBuilder;
import com.laundrybooking.model.Booking;
import com.laundrybooking.model.BookingStatus;
import com.laundrybooking.model.House;
import com.laundrybooking.model.LaundryRoom;
import com.laundrybooking.model.Response;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingHandler {

    private final ResponseBuilder responseBuilder;

    private final BookingDataAccessor bookingDataAccessor;

    private final LaundryRoomDataAccessor laundryRoomDataAccessor;

    private final HouseDataAccessor houseDataAccessor;

    private final BookingValidator bookingValidator;

    public BookingHandler(final BookingDataAccessor bookingDataAccessor, final LaundryRoomDataAccessor laundryRoomDataAccessor,
                          final HouseDataAccessor houseDataAccessor, final BookingValidator bookingValidator) {
        responseBuilder = new ResponseBuilder();
        this.bookingDataAccessor = bookingDataAccessor;
        this.laundryRoomDataAccessor = laundryRoomDataAccessor;
        this.houseDataAccessor = houseDataAccessor;
        this.bookingValidator = bookingValidator;
    }

    public Response book(final String laundryRoomId, final String houseId, final String bookingStartTimeUTC, final String bookingEndTimeUTC) {
        try {
            // Data validations
            final LaundryRoom laundryRoom = laundryRoomDataAccessor.read(laundryRoomId);
            if (laundryRoom == null) {
                return responseBuilder.buildErrorResponse("Invalid laundry room id", 400);
            }

            final House house = houseDataAccessor.read(houseId);
            if (house == null) {
                return responseBuilder.buildErrorResponse("Invalid house id", 400);
            }

            final String bookingTimeValidations = bookingValidator.validateBookingTimes(bookingStartTimeUTC, bookingEndTimeUTC, laundryRoom);
            if (bookingTimeValidations != null) {
                return responseBuilder.buildErrorResponse(bookingTimeValidations, 400);
            }

            // Can house book?
            final String canHouseBook = canHouseBookLaundrySlot(house);
            if (canHouseBook != null) {
                return responseBuilder.buildErrorResponse(canHouseBook, 400);
            }

            final Booking booking = Booking.builder()
                    .bookingStatus(BookingStatus.ACTIVE)
                    .bookingStartTimeUTC(Instant.parse(bookingStartTimeUTC))
                    .bookingEndTimeUTC(Instant.parse(bookingEndTimeUTC))
                    .laundryRoomId(Integer.parseInt(laundryRoomId))
                    .houseId(Integer.parseInt(houseId))
                    .build();
            bookingDataAccessor.create(booking);

            final Map<String, String> payloadMap = new HashMap<>();
            payloadMap.put("message", "Slot successfully booked");
            payloadMap.put("laundryRoomId", laundryRoomId);
            payloadMap.put("laundryRoomName", laundryRoom.name);
            payloadMap.put("bookingStartTimeUTC", bookingStartTimeUTC);
            payloadMap.put("bookingEndTimeUTC", bookingEndTimeUTC);

            final String jsonString = buildJsonString(payloadMap);
            return responseBuilder.buildSuccessResponse(jsonString);
        } catch (final Exception e) {
            e.printStackTrace();
            return responseBuilder.buildErrorResponse(HTTP_500_MESSAGE, 500);
        }
    }

    public List<Booking> listBookedTimes() {
        // Show booked times for the next 1 month
        return Collections.emptyList();
    }

    public Response cancelBooking(final String bookingId) {
        return responseBuilder.buildSuccessResponse("");
    }

    /**
     * Checks if a house can book a laundry slot.
     * Today this just checks if the user has an ACTIVE laundry booking and just lets the client know about the same
     *
     * @return null if the house can book a slot else returns the appropriate message
     */
    public String canHouseBookLaundrySlot(final House house) {
        final Booking booking = bookingDataAccessor.read(house, BookingStatus.ACTIVE);
        if (booking != null) {
            return "You already have an active booking starting " + booking.bookingStartTimeUTC.toString();
        }
        return null;
    }
}
