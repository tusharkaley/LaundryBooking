package com.laundrybooking.model;

import java.time.Instant;

import lombok.Builder;

@Builder
public class Booking {
    public String id;

    public int houseId;

    public int laundryRoomId;

    public Instant bookingStartTimeUTC;

    public Instant bookingEndTimeUTC;

    public BookingStatus bookingStatus;
}
