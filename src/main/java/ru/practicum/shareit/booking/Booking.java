package ru.practicum.shareit.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.constants.Constants;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {
    private Integer id;
    @NotNull(message = "dateFrom is mandatory")
    private LocalDateTime dateFrom;
    @NotNull(message = "dateTo is mandatory")
    private LocalDateTime dateTo;
    private Item item;
    private User booker;
    private Constants.Status status;

}
