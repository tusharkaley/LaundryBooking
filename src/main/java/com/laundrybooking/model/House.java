package com.laundrybooking.model;

import lombok.Builder;

@Builder
public class House {
    public int id;

    public String streetAddress;

    public String houseNumber;

    public String city;

    public String state;

    public String zipCode;

    public String contactNumber;
}
