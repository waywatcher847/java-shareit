package ru.practicum.common.item;

import lombok.*;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.request.ItemRequestDto;

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
    private ItemRequestDto request;
    private List<CommentDto> comments;
}
