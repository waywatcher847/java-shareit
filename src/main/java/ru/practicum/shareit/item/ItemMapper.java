package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoNew;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    public static ItemDto mapToItemDto(Item item) {

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable()).build();
    }

    public static Item mapToItem(ItemDtoNew newItemRequest) {

        return Item.builder()
                .name(newItemRequest.getName())
                .description(newItemRequest.getDescription())
                .available(newItemRequest.getAvailable())
                .owner(new User())
                .request(new ItemRequest()).build();
    }

    public static Item updateItemField(Item item, ItemDtoUpdate ItemDto) {
        if (ItemDto.hasName()) {
            item.setName(ItemDto.getName());
        }

        if (ItemDto.hasDescription()) {
            item.setDescription(ItemDto.getDescription());
        }

        if (ItemDto.hasAvailable()) {
            item.setAvailable(ItemDto.getAvailable());
        }
        return item;
    }
}
