package com.laundrybooking.accessor;

import com.laundrybooking.model.LaundryRoom;

public class LaundryRoomDataAccessor implements DataAccessor<LaundryRoom> {
    @Override public void create(final LaundryRoom laundryRoom) {

    }

    @Override public LaundryRoom read(final String id) {
        return new LaundryRoom();
    }

    @Override public void update() {

    }

    @Override public void delete() {

    }
}
