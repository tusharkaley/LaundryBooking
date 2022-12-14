package com.laundrybooking.accessor;

import com.laundrybooking.model.LaundryRoom;

/**
 * NO OP class for accessing the Laundry room data
 */
public class LaundryRoomDataAccessor implements DataAccessor<LaundryRoom> {
    @Override public void create(final LaundryRoom laundryRoom) {

    }

    @Override public LaundryRoom read(final String id) {
        // No Op data accessor
        return LaundryRoom.builder().build();
    }

    @Override public void update() {

    }

    @Override public void delete() {

    }
}
