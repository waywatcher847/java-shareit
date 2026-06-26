package ru.practicum.common.booking;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDtoRequest {

    @NotNull(message = "ID is mandatory")
    @Positive(message = "ID can't be negative")
    Integer itemId;

    @NotNull(message = "startDate is mandatory")
    @FutureOrPresent(message = "startDate can't be in the past")
    LocalDateTime start;

    @NotNull(message = "endDate is mandatory")
    @Future(message = "endDate can't be in the past")
    LocalDateTime end;

    @AssertTrue(message = "Dates should be chronological")
    public boolean isValid() {
        if (start == null || end == null) {
            return true; //почему-то проверятся до @NotNull
        }
        return start.isBefore(end);
    }
}