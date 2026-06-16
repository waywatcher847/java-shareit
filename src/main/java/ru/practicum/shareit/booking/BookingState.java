package ru.practicum.shareit.booking;

import java.util.Locale;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String state) {
        return switch (state.toUpperCase(Locale.ROOT)) {
            case "CURRENT" -> CURRENT;
            case "PAST" -> PAST;
            case "FUTURE" -> FUTURE;
            case "WAITING" -> WAITING;
            case "REJECTED" -> REJECTED;
            default -> ALL;
        };
    }
}
