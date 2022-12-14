package com.laundrybooking.handlers;

import static com.laundrybooking.handlers.BookingValidator.BOOKING_START_TIME_GT_END_TIME;
import static com.laundrybooking.handlers.BookingValidator.INVALID_HOUSE_ID;
import static com.laundrybooking.handlers.BookingValidator.INVALID_LAUNDRY_ROOM_ID;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.laundrybooking.accessor.HouseDataAccessor;
import com.laundrybooking.accessor.LaundryRoomDataAccessor;
import com.laundrybooking.model.House;
import com.laundrybooking.model.LaundryRoom;

import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingValidatorTest {

    private BookingValidator bookingValidator;

    @Mock
    private LaundryRoomDataAccessor laundryRoomDataAccessor;

    @Mock
    private HouseDataAccessor houseDataAccessor;

    @BeforeEach
    public void setup() {
        bookingValidator = new BookingValidator(laundryRoomDataAccessor, houseDataAccessor);
    }

    @Test
    public void test_validateBooking_success() {
        //Arrange
        final String laundryRoomId = "1";
        final String houseId = "2";
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final int startHour = LocalDateTime.ofInstant(bookingStartTimeUTC.minus(90, MINUTES), UTC).getHour();
        final int endHour = LocalDateTime.ofInstant(bookingEndTimeUTC.plus(90, MINUTES), UTC).getHour();
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .startHour(startHour)
                .endHour(endHour)
                .build();

        when(laundryRoomDataAccessor.read(laundryRoomId)).thenReturn(laundryRoom);
        when(houseDataAccessor.read(houseId)).thenReturn(House.builder().build());

        // Act
        final String actual = bookingValidator.validateBooking(laundryRoomId, houseId, bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString());

        // Assert
        assertNull(actual);
    }

    @Test
    public void test_validateBooking_invalidLaundryRoom() {
        //Arrange
        final String laundryRoomId = "1";
        final String houseId = "2";
        final String bookingStartTimeUTC = Instant.now().plus(10, MINUTES).toString();
        final String bookingEndTimeUTC = Instant.now().plus(60, MINUTES).toString();
        when(laundryRoomDataAccessor.read(laundryRoomId)).thenReturn(null);

        // Act
        final String actual = bookingValidator.validateBooking(laundryRoomId, houseId, bookingStartTimeUTC, bookingEndTimeUTC);

        // Assert
        assertEquals(INVALID_LAUNDRY_ROOM_ID, actual);
    }

    @Test
    public void test_validateBooking_invalidHouseId() {
        //Arrange
        final String laundryRoomId = "1";
        final String houseId = "2";
        final String bookingStartTimeUTC = Instant.now().plus(10, MINUTES).toString();
        final String bookingEndTimeUTC = Instant.now().plus(60, MINUTES).toString();
        when(laundryRoomDataAccessor.read(laundryRoomId)).thenReturn(LaundryRoom.builder().build());
        when(houseDataAccessor.read(houseId)).thenReturn(null);

        // Act
        final String actual = bookingValidator.validateBooking(laundryRoomId, houseId, bookingStartTimeUTC, bookingEndTimeUTC);

        // Assert
        assertEquals(INVALID_HOUSE_ID, actual);
    }

    @Test
    public void test_validateBookingTimes_success() {
        //Arrange
        final String laundryRoomId = "1";
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final int startHour = LocalDateTime.ofInstant(bookingStartTimeUTC.minus(90, MINUTES), UTC).getHour();
        final int endHour = LocalDateTime.ofInstant(bookingEndTimeUTC.plus(90, MINUTES), UTC).getHour();
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .startHour(startHour)
                .endHour(endHour)
                .build();

        // Act
        final String actual = bookingValidator.validateBookingTimes(bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), laundryRoom);

        // Assert
        assertNull(actual);
    }

    @Test
    public void test_validateBookingTimes_bookingStartHourInvalid() {
        //Arrange
        final String laundryRoomId = "1";
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final int startHour = LocalDateTime.ofInstant(bookingStartTimeUTC.plus(70, MINUTES), UTC).getHour();
        final int endHour = LocalDateTime.ofInstant(bookingEndTimeUTC.plus(90, MINUTES), UTC).getHour();
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .startHour(startHour)
                .endHour(endHour)
                .build();

        final String expected = "Slot outside valid booking hours. Rooms are bookable between " + laundryRoom.startHour + " and " + laundryRoom.endHour +
                " every day.";
        // Act
        final String actual = bookingValidator.validateBookingTimes(bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), laundryRoom);

        // Assert
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void test_validateBookingTimes_bookingEndHourInvalid() {
        //Arrange
        final String laundryRoomId = "1";
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final int startHour = LocalDateTime.ofInstant(bookingStartTimeUTC.minus(10, MINUTES), UTC).getHour();
        final int endHour = LocalDateTime.ofInstant(bookingEndTimeUTC.minus(70, MINUTES), UTC).getHour();
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .startHour(startHour)
                .endHour(endHour)
                .build();
        final String expected = "Slot outside valid booking hours. Rooms are bookable between " + laundryRoom.startHour + " and " + laundryRoom.endHour +
                " every day.";
        // Act
        final String actual = bookingValidator.validateBookingTimes(bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), laundryRoom);

        // Assert
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void test_validateBookingTimes_bookingStartGreaterThanBookingEnd() {
        //Arrange
        final String laundryRoomId = "1";
        final Instant bookingStartTimeUTC = Instant.now().plus(90, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final int startHour = LocalDateTime.ofInstant(bookingStartTimeUTC.minus(90, MINUTES), UTC).getHour();
        final int endHour = LocalDateTime.ofInstant(bookingEndTimeUTC.plus(90, MINUTES), UTC).getHour();
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .startHour(startHour)
                .endHour(endHour)
                .build();

        // Act
        final String actual = bookingValidator.validateBookingTimes(bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), laundryRoom);

        // Assert
        assertNotNull(actual);
        assertEquals(BOOKING_START_TIME_GT_END_TIME, actual);
    }

    @Test
    public void test_validateBookingTimes_outsideValidBookingWindow() {
        //Arrange
        final String laundryRoomId = "1";
        final Instant bookingStartTimeUTC = Instant.now().plus(90, DAYS);
        final Instant bookingEndTimeUTC = Instant.now().plus(91, DAYS);
        final int startHour = LocalDateTime.ofInstant(bookingStartTimeUTC.minus(90, MINUTES), UTC).getHour();
        final int endHour = LocalDateTime.ofInstant(bookingEndTimeUTC.plus(90, MINUTES), UTC).getHour();
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .startHour(startHour)
                .endHour(endHour)
                .build();
        final String expected = "Booking too far out in the future. Slots can be booked only for the next " + laundryRoom.bookingWindow + " days";
        // Act
        final String actual = bookingValidator.validateBookingTimes(bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), laundryRoom);

        // Assert
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void test_validateBookingTimes_slotTooSmall() {
        //Arrange
        final String laundryRoomId = "1";
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(60, MINUTES);
        final int startHour = LocalDateTime.ofInstant(bookingStartTimeUTC.minus(90, MINUTES), UTC).getHour();
        final int endHour = LocalDateTime.ofInstant(bookingEndTimeUTC.plus(90, MINUTES), UTC).getHour();
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(70)
                .maxSlotLength(150)
                .startHour(startHour)
                .endHour(endHour)
                .build();
        final String expected = "Booking slot cannot be smaller than " + laundryRoom.minSlotLength + " minutes";
        // Act
        final String actual = bookingValidator.validateBookingTimes(bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), laundryRoom);

        // Assert
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void test_validateBookingTimes_slotTooBig() {
        //Arrange
        final String laundryRoomId = "1";
        final Instant bookingStartTimeUTC = Instant.now().plus(10, MINUTES);
        final Instant bookingEndTimeUTC = Instant.now().plus(2, DAYS);
        final int startHour = LocalDateTime.ofInstant(bookingStartTimeUTC.minus(90, MINUTES), UTC).getHour();
        final int endHour = LocalDateTime.ofInstant(bookingEndTimeUTC.plus(90, MINUTES), UTC).getHour();
        final LaundryRoom laundryRoom = LaundryRoom.builder()
                .id(Integer.parseInt(laundryRoomId))
                .bookingWindow(30)
                .minSlotLength(10)
                .maxSlotLength(90)
                .startHour(startHour)
                .endHour(endHour)
                .build();
        final String expected = "Booking slot cannot be greater than " + laundryRoom.maxSlotLength + " minutes";
        // Act
        final String actual = bookingValidator.validateBookingTimes(bookingStartTimeUTC.toString(), bookingEndTimeUTC.toString(), laundryRoom);

        // Assert
        assertNotNull(actual);
        assertEquals(expected, actual);
    }
}
