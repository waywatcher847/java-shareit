package ru.practicum.shareit.item;

import org.mapstruct.*;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.item.ItemDto;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemMapper {

    @Mapping(target = "requestId", source = "requestId")
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    ItemDto toDto(Item item);

    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "requestId", source = "requestId")
    Item toEntity(ItemDto itemDto);

    List<ItemDto> toDtoList(List<Item> items);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "requestId", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updateItemFromDto(ItemDto itemDto, @MappingTarget Item item);

    @Named("toDtoWithDetails")
    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "name", source = "item.name")
    @Mapping(target = "description", source = "item.description")
    @Mapping(target = "available", source = "item.available")
    @Mapping(target = "userId", source = "item.userId")
    @Mapping(target = "requestId", source = "item.requestId")
    @Mapping(target = "lastBooking", source = "lastBooking")
    @Mapping(target = "nextBooking", source = "nextBooking")
    @Mapping(target = "comments", source = "comments")
    ItemDto toDto(Item item, BookingDto lastBooking, BookingDto nextBooking, List<CommentDto> comments);
}