package com.laundrybooking.handlers;

import static com.laundrybooking.handlers.BookingHandler.INVALID_BOOKING_MESSAGE;
import static com.laundrybooking.handlers.BookingHandler.SLOT_ALREADY_BOOKED;
import static com.laundrybooking.handlers.BookingHandler.SLOT_SUCCESSFULLY_BOOKED;
import static com.laundrybooking.handlers.BookingHandler.SLOT_SUCCESSFULLY_CANCELLED;
import static com.laundrybooking.handlers.BookingValidator.BOOKING_START_TIME_GT_END_TIME;
import static com.laundrybooking.model.BookingStatus.ACTIVE;
import static com.laundrybooking.model.BookingStatus.CANCELLED;
import static com.laundrybooking.utils.Constants.BOOKING_END_TIME_UTC_KEY;
import static com.laundrybooking.utils.Constants.BOOKING_START_TIME_UTC_KEY;
import static com.laundrybooking.utils.Constants.BOOKING_STATUS_KEY;
import static com.laundrybooking.utils.Constants.HTTP_400;
import static com.laundrybooking.utils.Constants.HTTP_500;
import static com.laundrybooking.utils.Constants.HTTP_500_MESSAGE;
import static com.laundrybooking.utils.Constants.LAUNDRY_ROOM_ID_KEY;
import static com.laundrybooking.utils.Constants.LAUNDRY_ROOM_NAME_KEY;
import static com.laundrybooking.utils.Constants.MESSAGE_KEY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Collections.EMPTY_LIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.laundrybooking.accessor.BookingDataAccessor;
import com.laundrybooking.accessor.HouseDataAccessor;
import com.laundrybooking.accessor.LaundryRoomDataAccessor;
import com.laundrybooking.model.Booking;
import com.laundrybooking.model.House;
import com.laundrybooking.model.LaundryRoom;
import com.laundrybooking.model.Response;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingHandlerTest {

    private BookingHandler bookingHandler;

    @Mock
    private LaundryRoomDataAccessor laundryRoomDataAccessor;

    @Mock
    private HouseDataAccessor houseDataAccessor;

    @Mock
    private BookingDataAccessor bookingDataAccessor;

    @Mock
    private BookingValidator bookingValidator;

    @BeforeEach
    public void setup() {
        bookingHandler = new BookingHandler(bookingDataAccessor, laundryRoomDataAccessor, houseDataAccessor, bookingValidator);
    }

    @Test
    public void test_book_success() {
        // Arrange
        final String laundryRoomId = "1";
        final String houseId = "2";
        final House house = House.builder().build();
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .name("Room 1")
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .build();

        when(bookingValidator.validateBooking(laundryRoomId, houseId, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString())).thenReturn(null);
        when(bookingDataAccessor.read(house, ACTIVE)).thenReturn(null);
        when(bookingDataAccessor.read(laundryRoom, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), ACTIVE)).thenReturn(null);
        when(houseDataAccessor.read(houseId)).thenReturn(house);
        when(laundryRoomDataAccessor.read(laundryRoomId)).thenReturn(laundryRoom);
        doNothing().when(bookingDataAccessor).create(any());

        // Act
        final Response actual = bookingHandler.book(laundryRoomId, houseId, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString());

        // Assert
        final Gson gson = new Gson();
        final Map payloadMap = gson.fromJson(actual.getPayload(), Map.class);
        assertNotNull(actual);
        assertEquals(SLOT_SUCCESSFULLY_BOOKED, payloadMap.get(MESSAGE_KEY));
        assertEquals(laundryRoomId, payloadMap.get(LAUNDRY_ROOM_ID_KEY));
        assertEquals("Room 1", payloadMap.get(LAUNDRY_ROOM_NAME_KEY));
        assertEquals(bookingStartTimeUTC.toString(), payloadMap.get(BOOKING_START_TIME_UTC_KEY));
        assertEquals(bookingEndTimeUTC.toString(), payloadMap.get(BOOKING_END_TIME_UTC_KEY));
    }

    @Test
    public void test_book_bookingValidationsFailed_errorResponse() {
        // Arrange
        final String laundryRoomId = "1";
        final String houseId = "2";
        final House house = House.builder().build();
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .name("Room 1")
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .build();

        when(bookingValidator.validateBooking(laundryRoomId, houseId, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString())).thenReturn(
                BOOKING_START_TIME_GT_END_TIME);

        // Act
        final Response actual = bookingHandler.book(laundryRoomId, houseId, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString());

        // Assert
        assertEquals(HTTP_400, actual.getResponseCode());
        assertEquals(BOOKING_START_TIME_GT_END_TIME, actual.getErrorMessage());
    }

    @Test
    public void test_book_houseCannotBookSlot_errorResponse() {
        // Arrange
        final String laundryRoomId = "1";
        final String houseId = "2";
        final House house = House.builder().build();
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .name("Room 1")
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .build();

        final Booking booking = Booking.builder()
                .bookingStartTimeUTC(Instant.now().plus(10, DAYS))
                .build();
        final String expected = "You already have an active booking starting " + booking.bookingStartTimeUTC.toString();
        when(bookingValidator.validateBooking(laundryRoomId, houseId, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString())).thenReturn(null);
        when(houseDataAccessor.read(houseId)).thenReturn(house);
        when(laundryRoomDataAccessor.read(laundryRoomId)).thenReturn(laundryRoom);
        when(bookingDataAccessor.read(house, ACTIVE)).thenReturn(booking);

        // Act
        final Response actual = bookingHandler.book(laundryRoomId, houseId, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString());

        // Assert
        assertEquals(HTTP_400, actual.getResponseCode());
        assertEquals(expected, actual.getErrorMessage());
    }

    @Test
    public void test_book_bookingCreateFailed_500Response() {
        // Arrange
        final String laundryRoomId = "1";
        final String houseId = "2";
        final House house = House.builder().build();
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .name("Room 1")
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .build();

        when(bookingValidator.validateBooking(laundryRoomId, houseId, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString())).thenReturn(null);
        when(bookingDataAccessor.read(house, ACTIVE)).thenReturn(null);
        when(bookingDataAccessor.read(laundryRoom, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), ACTIVE)).thenReturn(null);
        when(houseDataAccessor.read(houseId)).thenReturn(house);
        when(laundryRoomDataAccessor.read(laundryRoomId)).thenReturn(laundryRoom);
        doThrow(new RuntimeException()).when(bookingDataAccessor).create(any());

        // Act
        final Response actual = bookingHandler.book(laundryRoomId, houseId, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString());

        // Assert
        assertNotNull(actual);
        assertEquals(HTTP_500_MESSAGE, actual.getErrorMessage());
        assertEquals(HTTP_500, actual.getResponseCode());
    }

    @Test
    public void test_listBookedTimes_success() {
        // Arrange
        final List<Booking> bookings = generateBookings();
        when(bookingDataAccessor.read(any(Instant.class), any(Instant.class), eq(ACTIVE))).thenReturn(bookings);

        // Act
        final Response actual = bookingHandler.listBookedTimes();

        // Assert
        final Gson gson = new Gson();
        final List payloadList = gson.fromJson(actual.getPayload(), List.class);
        assertEquals(bookings.size(), payloadList.size());
    }

    @Test
    public void test_listBookedTimes_emptyList() {
        // Arrange
        final List<Booking> bookings = EMPTY_LIST;
        when(bookingDataAccessor.read(any(Instant.class), any(Instant.class), eq(ACTIVE))).thenReturn(bookings);

        // Act
        final Response actual = bookingHandler.listBookedTimes();

        // Assert
        final Gson gson = new Gson();
        final List payloadList = gson.fromJson(actual.getPayload(), List.class);
        assertEquals(EMPTY_LIST, payloadList);
    }

    @Test
    public void test_listBookedTimes_HTTP500() {
        // Arrange
        final List<Booking> bookings = EMPTY_LIST;
        when(bookingDataAccessor.read(any(Instant.class), any(Instant.class), eq(ACTIVE))).thenThrow(new RuntimeException());

        // Act
        final Response actual = bookingHandler.listBookedTimes();

        // Assert
        assertEquals(HTTP_500_MESSAGE, actual.getErrorMessage());
        assertEquals(HTTP_500, actual.getResponseCode());
    }

    @Test
    public void test_cancel_success() {
        // Arrange
        final String bookingId = UUID.randomUUID().toString();
        final String houseId = "2";
        final Booking booking = Booking.builder().build();
        when(bookingDataAccessor.read(bookingId, houseId, ACTIVE)).thenReturn(booking);
        doNothing().when(bookingDataAccessor).update(bookingId, BOOKING_STATUS_KEY, CANCELLED.toString());

        // Act
        final Response actual = bookingHandler.cancelBooking(bookingId, houseId);

        // Assert
        assertEquals(SLOT_SUCCESSFULLY_CANCELLED, actual.getPayload());
    }

    @Test
    public void test_cancel_failure() {
        // Arrange
        final String bookingId = UUID.randomUUID().toString();
        final String houseId = "2";
        final Booking booking = Booking.builder().build();
        when(bookingDataAccessor.read(bookingId, houseId, ACTIVE)).thenReturn(null);

        // Act
        final Response actual = bookingHandler.cancelBooking(bookingId, houseId);

        // Assert
        assertEquals(INVALID_BOOKING_MESSAGE, actual.getErrorMessage());
        assertEquals(HTTP_400, actual.getResponseCode());
    }

    @Test
    public void test_cancel_HTTP500() {
        // Arrange
        final String bookingId = UUID.randomUUID().toString();
        final String houseId = "2";
        final Booking booking = Booking.builder().build();
        when(bookingDataAccessor.read(bookingId, houseId, ACTIVE)).thenThrow(new RuntimeException());

        // Act
        final Response actual = bookingHandler.cancelBooking(bookingId, houseId);

        // Assert
        assertEquals(HTTP_500_MESSAGE, actual.getErrorMessage());
        assertEquals(HTTP_500, actual.getResponseCode());
    }

    @Test
    public void test_canBookLaundrySlot_canBook() {
        // Arrange
        final String laundryRoomId = "1";
        final House house = House.builder().build();
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .build();

        when(bookingDataAccessor.read(house, ACTIVE)).thenReturn(null);
        when(bookingDataAccessor.read(laundryRoom, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), ACTIVE)).thenReturn(null);

        // Act
        final String actual = bookingHandler.canBookLaundrySlot(house, laundryRoom, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString());

        // Assert
        assertNull(actual);
    }

    @Test
    public void test_canBookLaundrySlot_HouseHasActiveBooking_cannotBook() {
        // Arrange
        final String laundryRoomId = "1";
        final House house = House.builder().build();
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .build();
        final Booking booking = Booking.builder()
                .bookingStartTimeUTC(Instant.now().plus(10, DAYS))
                .build();
        final String expected = "You already have an active booking starting " + booking.bookingStartTimeUTC.toString();
        when(bookingDataAccessor.read(house, ACTIVE)).thenReturn(booking);

        // Act
        final String actual = bookingHandler.canBookLaundrySlot(house, laundryRoom, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString());

        // Assert
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void test_canBookLaundrySlot_slotAlreadyBooked_cannotBook() {
        // Arrange
        final String laundryRoomId = "1";
        final House house = House.builder().build();
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .build();
        final Booking booking = Booking.builder().build();
        when(bookingDataAccessor.read(house, ACTIVE)).thenReturn(null);
        when(bookingDataAccessor.read(laundryRoom, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), ACTIVE)).thenReturn(booking);

        // Act
        final String actual = bookingHandler.canBookLaundrySlot(house, laundryRoom, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString());

        // Assert
        assertNotNull(actual);
        assertEquals(SLOT_ALREADY_BOOKED, actual);
    }

    List<Booking> generateBookings() {
        return Arrays.asList(
                Booking.builder()
                        .bookingStartTimeUTC(Instant.now().plus(10, HOURS))
                        .bookingEndTimeUTC(Instant.now().plus(12, HOURS))
                        .laundryRoomId(1)
                        .build(),
                Booking.builder()
                        .bookingStartTimeUTC(Instant.now().plus(90, MINUTES))
                        .bookingEndTimeUTC(Instant.now().plus(150, MINUTES))
                        .laundryRoomId(2)
                        .build(),
                Booking.builder()
                        .bookingStartTimeUTC(Instant.now().plus(10, MINUTES))
                        .bookingEndTimeUTC(Instant.now().plus(90, MINUTES))
                        .laundryRoomId(1)
                        .build(),
                Booking.builder()
                        .bookingStartTimeUTC(Instant.now().plus(3, HOURS))
                        .bookingEndTimeUTC(Instant.now().plus(4, HOURS))
                        .laundryRoomId(1)
                        .build(),
                Booking.builder()
                        .bookingStartTimeUTC(Instant.now().plus(90, HOURS))
                        .bookingEndTimeUTC(Instant.now().plus(92, HOURS))
                        .laundryRoomId(2)
                        .build()
        );
    }
}
