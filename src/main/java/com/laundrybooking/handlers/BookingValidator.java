package com.laundrybooking.handlers;

import static java.time.ZoneOffset.UTC;

import com.laundrybooking.accessor.HouseDataAccessor;
import com.laundrybooking.accessor.LaundryRoomDataAccessor;
import com.laundrybooking.model.House;
import com.laundrybooking.model.LaundryRoom;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class BookingValidator {

    static final String BOOKING_START_TIME_GT_END_TIME = "Booking start time cannot be greater than end time";

    static final String INVALID_LAUNDRY_ROOM_ID = "Invalid laundry room id";

    static final String INVALID_HOUSE_ID = "Invalid house id";

    private final LaundryRoomDataAccessor laundryRoomDataAccessor;

    private final HouseDataAccessor houseDataAccessor;

    public BookingValidator(final LaundryRoomDataAccessor laundryRoomDataAccessor,
                            final HouseDataAccessor houseDataAccessor) {
        this.laundryRoomDataAccessor = laundryRoomDataAccessor;
        this.houseDataAccessor = houseDataAccessor;
    }

    String validateBooking(final String laundryRoomId, final String houseId,
                           final String bookingStartDateTimeUTC, final String bookingEndDateTimeUTC) {
        final LaundryRoom laundryRoom = laundryRoomDataAccessor.read(laundryRoomId);
        if (laundryRoom == null) {
            return INVALID_LAUNDRY_ROOM_ID;
        }
        final House house = houseDataAccessor.read(houseId);
        if (house == null) {
            return INVALID_HOUSE_ID;
        }

        final String bookingTimeValidations = validateBookingTimes(bookingStartDateTimeUTC, bookingEndDateTimeUTC, laundryRoom);
        if (bookingTimeValidations != null) {
            return bookingTimeValidations;
        }
        return null;
    }

    /**
     * Validates the booking times for a laundry room. Validations performed
     * - Laundry slots should be within the bookable times
     * - Booking end time should be after booking start time
     * - Booking cannot be booked too far out in the future
     * - Booking cannot be less than the minSlotLength for the laundry room
     * - Booking cannot be greater than the maxSlotLength for the laundry room
     *
     * @param bookingStartTimeUTCStr
     * @param bookingEndTimeUTCStr
     * @param laundryRoom
     * @return
     */
    String validateBookingTimes(final String bookingStartTimeUTCStr, final String bookingEndTimeUTCStr, final LaundryRoom laundryRoom) {
        final Instant bookingStartTimeUTC = Instant.parse(bookingStartTimeUTCStr);
        final Instant bookingEndTimeUTC = Instant.parse(bookingEndTimeUTCStr);
        final int bookingStartHour = bookingStartTimeUTC.atZone(UTC).getHour();
        final int bookingEndHour = bookingEndTimeUTC.atZone(UTC).getHour();

        if (bookingStartHour < laundryRoom.startHour || bookingEndHour > laundryRoom.endHour) {
            return "Slot outside valid booking hours. Rooms are bookable between " + laundryRoom.startHour + " and " + laundryRoom.endHour +
                    " every day.";
        }

        if (bookingStartTimeUTC.isAfter(bookingEndTimeUTC)) {
            return BOOKING_START_TIME_GT_END_TIME;
        }

        if (laundryRoom.bookingWindow < getDaysBetween(Instant.now(), bookingStartTimeUTC)) {
            return "Booking too far out in the future. Slots can be booked only for the next " + laundryRoom.bookingWindow + " days";
        }

        if (laundryRoom.minSlotLength > getMinutesBetween(bookingStartTimeUTC, bookingEndTimeUTC)) {
            return "Booking slot cannot be smaller than " + laundryRoom.minSlotLength + " minutes";
        }

        if (laundryRoom.maxSlotLength < getMinutesBetween(bookingStartTimeUTC, bookingEndTimeUTC)) {
            return "Booking slot cannot be greater than " + laundryRoom.maxSlotLength + " minutes";
        }
        return null;
    }

    private static long getMinutesBetween(final Instant fromInstant, final Instant toInstant) {
        final Duration duration = Duration.between(fromInstant, toInstant);
        final long seconds = duration.get(ChronoUnit.SECONDS);
        return seconds / 60;
    }

    private static long getDaysBetween(final Instant fromInstant, final Instant toInstant) {
        final Duration duration = Duration.between(fromInstant, toInstant);
        final long seconds = duration.get(ChronoUnit.SECONDS);
        return seconds / 60 / 60 / 24;
    }
}
