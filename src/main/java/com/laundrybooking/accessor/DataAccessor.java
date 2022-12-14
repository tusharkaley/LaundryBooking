package com.laundrybooking.accessor;

public interface DataAccessor<T> {
    void create(T object);
    T read(String id);
    void update();
    void delete();
}
