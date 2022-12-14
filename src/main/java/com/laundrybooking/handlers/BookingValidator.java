package com.laundrybooking.handlers;

import com.laundrybooking.model.LaundryRoom;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class BookingValidator {

    public static final String BOOKING_START_TIME_GT_END_TIME = "Booking start time cannot be greater than end time";
    String validateBookingTimes(final String bookingStartTimeUTCStr, final String bookingEndTimeUTCStr, final LaundryRoom laundryRoom) {
        final Instant bookingStartTimeUTC = Instant.parse(bookingStartTimeUTCStr);
        final Instant bookingEndTimeUTC = Instant.parse(bookingEndTimeUTCStr);
        final int bookingStartHour = bookingStartTimeUTC.atZone(ZoneId.of(laundryRoom.timeZone)).getHour();
        final int bookingEndHour = bookingEndTimeUTC.atZone(ZoneId.of(laundryRoom.timeZone)).getHour();
        if (bookingStartHour<laundryRoom.startHour || bookingEndHour > laundryRoom.endHour) {
            return "Slot outside valid booking hours. Rooms are bookable between "+ laundryRoom.startHour +" and "+ laundryRoom.endHour +" every day.";
        }

        if (bookingStartTimeUTC.isAfter(bookingEndTimeUTC)) {
            return BOOKING_START_TIME_GT_END_TIME;
        }

        if (laundryRoom.minSlotLength > getMinutesBetween(bookingStartTimeUTC, bookingEndTimeUTC)) {
            return "Booking slot cannot be smaller than "+ laundryRoom.minSlotLength +" minutes";
        }
        return null;
    }

    private static long getMinutesBetween(final Instant fromInstant, final Instant toInstant) {
        final Duration duration = Duration.between(fromInstant, toInstant);
        final long seconds = duration.get(ChronoUnit.SECONDS);
        return seconds/60;
    }
}
