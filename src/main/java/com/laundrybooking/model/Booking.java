package com.laundrybooking.model;

import java.time.Instant;

public class Booking {
    private String id;

    private int houseId;

    private int laundryRoomId;

    private Instant bookingStartTimeUTC;

    private Instant bookingEndTimeUTC;

    private BookingStatus bookingStatus;
}
