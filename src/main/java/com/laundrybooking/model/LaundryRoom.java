package com.laundrybooking.model;

import lombok.Builder;

@Builder
public class LaundryRoom {
    public int id;

    public String name;

    public int startHour;

    public int endHour;

    public int minSlotLength;

    public int maxSlotLength;

    public int bookingWindow;
}
