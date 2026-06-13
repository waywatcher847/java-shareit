package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingDtoUpdate {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    @FutureOrPresent(message = "Date can't be in the past")
    private LocalDateTime start;

    @Future(message =  "Date must be in the future")
    private LocalDateTime end;

    @NotNull
    private Integer item;

    @AssertTrue(message = "Dates should be chronological")
    public boolean isValid() {
        if (start == null || end == null) {
            return false;
        }
        return start.isBefore(end);
    }
}
