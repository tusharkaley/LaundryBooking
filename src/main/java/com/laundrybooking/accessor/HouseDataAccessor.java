package com.laundrybooking.accessor;

import com.laundrybooking.model.House;

/**
 * NO OP class for accessing the house data
 */
public class HouseDataAccessor implements DataAccessor<House>{
    @Override public void create(final House house) {

    }

    @Override public House read(final String id) {
        // No Op data accessor
        return House.builder().build();
    }

    @Override public void update() {

    }

    @Override public void delete() {

    }
}
