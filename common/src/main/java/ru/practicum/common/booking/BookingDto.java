package ru.practicum.common.booking;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.user.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {

    Integer id;
    LocalDateTime start;
    LocalDateTime end;
    UserDto booker;
    ItemDto item;
    BookingStatus status;
}
