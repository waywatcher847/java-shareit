package ru.practicum.shareit.booking.model;

import java.time.LocalDateTime;

public interface BookingNext {
    Integer getId();

    LocalDateTime getNextBooking();
}
