package ru.practicum.common.item;
import lombok.*;

import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.user.UserDto;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Integer id;
    private String name;
    private String description;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private Boolean available;
    private UserDto owner;
    private List<CommentDto> comments;
}
