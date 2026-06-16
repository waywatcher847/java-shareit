package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoNew;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

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
                .request(new ItemRequest())
                .build();
    }

    public static Item updateItemField(Item item, ItemDtoUpdate itemDto) {
        if (itemDto.hasName()) {
            item.setName(itemDto.getName());
        }

        if (itemDto.hasDescription()) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.hasAvailable()) {
            item.setAvailable(itemDto.getAvailable());
        }
        return item;
    }

    public static ItemDtoOwner mapToItemOwnerDto(Item item) {
        return ItemDtoOwner.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }
}
