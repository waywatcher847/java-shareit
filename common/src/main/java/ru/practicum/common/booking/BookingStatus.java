package ru.practicum.common.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum BookingStatus {
    ALL("ALL"),
    CURRENT("CURRENT"),
    WAITING("WAITING"),
    FUTURE("FUTURE"),
    PAST("PAST"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CANCELED("CANCELED");

    private final String status;

    public static Optional<BookingStatus> from(String stringState) {
        for (BookingStatus state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}