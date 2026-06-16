package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class ItemDtoOwner {
    private Integer id;
    private String name;
    private String description;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private Boolean available;
    private ItemRequest request;
    private List<CommentDto> comments;
}
