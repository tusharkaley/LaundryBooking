package com.laundrybooking.handlers;

import static com.laundrybooking.model.BookingStatus.ACTIVE;
import static com.laundrybooking.model.BookingStatus.CANCELLED;
import static com.laundrybooking.utils.Constants.BOOKING_END_TIME_KEY;
import static com.laundrybooking.utils.Constants.BOOKING_END_TIME_UTC_KEY;
import static com.laundrybooking.utils.Constants.BOOKING_START_TIME_KEY;
import static com.laundrybooking.utils.Constants.BOOKING_START_TIME_UTC_KEY;
import static com.laundrybooking.utils.Constants.BOOKING_STATUS_KEY;
import static com.laundrybooking.utils.Constants.HTTP_400;
import static com.laundrybooking.utils.Constants.HTTP_500;
import static com.laundrybooking.utils.Constants.HTTP_500_MESSAGE;
import static com.laundrybooking.utils.Constants.LAUNDRY_ROOM_ID_KEY;
import static com.laundrybooking.utils.Constants.LAUNDRY_ROOM_KEY;
import static com.laundrybooking.utils.Constants.LAUNDRY_ROOM_NAME_KEY;
import static com.laundrybooking.utils.Constants.MESSAGE_KEY;
import static com.laundrybooking.utils.JsonUtils.buildJsonString;

import com.google.gson.Gson;
import com.laundrybooking.accessor.BookingDataAccessor;
import com.laundrybooking.accessor.HouseDataAccessor;
import com.laundrybooking.accessor.LaundryRoomDataAccessor;
import com.laundrybooking.builder.ResponseBuilder;
import com.laundrybooking.model.Booking;
import com.laundrybooking.model.House;
import com.laundrybooking.model.LaundryRoom;
import com.laundrybooking.model.Response;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BookingHandler {

    private static final int LIST_BOOKED_TIMES_WINDOW = 30;

    static final String SLOT_ALREADY_BOOKED = "Slot already booked! Please try another slot";

    static final String SLOT_SUCCESSFULLY_BOOKED = "Laundry slot successfully booked";

    static final String SLOT_SUCCESSFULLY_CANCELLED = "Laundry slot successfully cancelled";

    static final String INVALID_BOOKING_MESSAGE = "Invalid booking id or booking not active";

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

    /**
     * Books a laundry slot for a house given start and end date times and laundry room id
     *
     * @param laundryRoomId
     * @param houseId
     * @param bookingStartDateTimeUTC
     * @param bookingEndDateTimeUTC
     * @return
     */
    public Response book(final String laundryRoomId, final String houseId, final String bookingStartDateTimeUTC, final String bookingEndDateTimeUTC) {
        try {
            // Data validations
            final String validationResult = bookingValidator.validateBooking(laundryRoomId, houseId, bookingStartDateTimeUTC, bookingEndDateTimeUTC);
            if (validationResult != null) {
                return responseBuilder.buildErrorResponse(validationResult, HTTP_400);
            }

            final House house = houseDataAccessor.read(houseId);
            final LaundryRoom laundryRoom = laundryRoomDataAccessor.read(laundryRoomId);

            // Can house book slot?
            final String canHouseBook = canBookLaundrySlot(house, laundryRoom, bookingStartDateTimeUTC, bookingEndDateTimeUTC);
            if (canHouseBook != null) {
                return responseBuilder.buildErrorResponse(canHouseBook, HTTP_400);
            }

            final Booking booking = Booking.builder()
                    .bookingStatus(ACTIVE)
                    .bookingStartTimeUTC(Instant.parse(bookingStartDateTimeUTC))
                    .bookingEndTimeUTC(Instant.parse(bookingEndDateTimeUTC))
                    .laundryRoomId(Integer.parseInt(laundryRoomId))
                    .houseId(Integer.parseInt(houseId))
                    .build();
            bookingDataAccessor.create(booking);

            final Map<String, String> payloadMap = new HashMap<>();
            payloadMap.put(MESSAGE_KEY, SLOT_SUCCESSFULLY_BOOKED);
            payloadMap.put(LAUNDRY_ROOM_ID_KEY, laundryRoomId);
            payloadMap.put(LAUNDRY_ROOM_NAME_KEY, laundryRoom.name);
            payloadMap.put(BOOKING_START_TIME_UTC_KEY, bookingStartDateTimeUTC);
            payloadMap.put(BOOKING_END_TIME_UTC_KEY, bookingEndDateTimeUTC);

            final String jsonString = buildJsonString(payloadMap);
            return responseBuilder.buildSuccessResponse(jsonString);
        } catch (final Exception e) {
            e.printStackTrace();
            return responseBuilder.buildErrorResponse(HTTP_500_MESSAGE, HTTP_500);
        }
    }

    /**
     * Lists booked times for the number of days configured in LIST_BOOKED_TIMES_WINDOW
     *
     * @return
     */
    public Response listBookedTimes() {
        // Show booked times for the LIST_BOOKED_TIMES_WINDOW
        try {
            final List<Booking> activeBookings = bookingDataAccessor.read(Instant.now(),
                    Instant.now().plus(LIST_BOOKED_TIMES_WINDOW, ChronoUnit.DAYS), ACTIVE);
            final List<Map<String, String>> bookedTimesList = activeBookings.stream().map(booking -> {
                final Map<String, String> map = new HashMap<>();
                map.put(BOOKING_START_TIME_KEY, booking.bookingStartTimeUTC.toString());
                map.put(BOOKING_END_TIME_KEY, booking.bookingEndTimeUTC.toString());
                map.put(LAUNDRY_ROOM_KEY, Integer.toString(booking.laundryRoomId));
                return map;
            }).collect(Collectors.toList());
            final String payload = new Gson().toJson(bookedTimesList);
            return responseBuilder.buildSuccessResponse(payload);
        } catch (final Exception e) {
            e.printStackTrace();
            return responseBuilder.buildErrorResponse(HTTP_500_MESSAGE, HTTP_500);
        }
    }

    /**
     * Cancels a booking provided the booking id represents an ACTIVE booking for the houseId
     *
     * @param bookingId
     * @return
     */

    public Response cancelBooking(final String bookingId, final String houseId) {
        try {
            final Booking booking = bookingDataAccessor.read(bookingId, houseId, ACTIVE);
            if (booking == null) {
                return responseBuilder.buildErrorResponse(INVALID_BOOKING_MESSAGE, HTTP_400);
            }
            bookingDataAccessor.update(bookingId, BOOKING_STATUS_KEY, CANCELLED.toString());
            return responseBuilder.buildSuccessResponse(SLOT_SUCCESSFULLY_CANCELLED);
        } catch (final Exception e) {
            e.printStackTrace();
            return responseBuilder.buildErrorResponse(HTTP_500_MESSAGE, HTTP_500);
        }
    }

    /**
     * Checks if a house can book a laundry slot.
     * Checks performed-
     * - If the user has an ACTIVE laundry booking
     * - If the requested slot has already been booked
     *
     * @return null if the house can book a slot else returns the appropriate message
     */
    String canBookLaundrySlot(final House house, final LaundryRoom laundryRoom, final String bookingStartDateTimeUTC,
                              final String bookingEndDateTimeUTC) {
        Booking booking = bookingDataAccessor.read(house, ACTIVE);
        if (booking != null) {
            return "You already have an active booking starting " + booking.bookingStartTimeUTC.toString();
        }

        booking = bookingDataAccessor.read(laundryRoom, bookingStartDateTimeUTC, bookingEndDateTimeUTC, ACTIVE);
        if (booking != null) {
            return SLOT_ALREADY_BOOKED;
        }
        return null;
    }
}
