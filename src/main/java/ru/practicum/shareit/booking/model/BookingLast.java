package ru.practicum.shareit.booking.model;

import java.time.LocalDateTime;

public interface BookingLast {
    Integer getId();

    LocalDateTime getLastBooking();
}
