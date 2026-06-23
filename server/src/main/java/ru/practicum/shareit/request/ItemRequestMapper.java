package ru.practicum.shareit.request;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.common.item.ItemResponseDto;
import ru.practicum.common.request.ItemRequestDto;

import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ItemRequestMapper {

    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "userId", ignore = true)
    ItemRequest toItemRequest(ItemRequestDto itemRequestDto);

    default ItemRequestDto toItemRequestDtoWithItems(ItemRequest itemRequest, List<ItemResponseDto> items) {
        ItemRequestDto dto = toItemRequestDto(itemRequest);
        dto.setItems(items);
        return dto;
    }
}