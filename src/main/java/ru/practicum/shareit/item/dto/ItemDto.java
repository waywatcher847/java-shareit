package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.UserDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class ItemDto {
    private Integer id;
    private String name;
    private String description;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private Boolean available;
    private UserDto owner;
    private ItemRequest request;
    private List<CommentDto> comments;
}

