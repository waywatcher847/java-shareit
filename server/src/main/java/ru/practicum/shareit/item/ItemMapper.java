package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.item.ItemDtoRequest;
import ru.practicum.common.item.ItemDtoOwner;
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

    public static Item mapToItem(ItemDtoRequest newItemRequest) {

        return Item.builder()
                .name(newItemRequest.getName())
                .description(newItemRequest.getDescription())
                .available(newItemRequest.getAvailable())
                .owner(new User())
                .request(new ItemRequest())
                .build();
    }

    public static Item updateItemField(Item item, ItemDtoRequest itemDto) {

        if (!(itemDto.getName() == null || itemDto.getName().isBlank())) {
            item.setName(itemDto.getName());
        }

        if (!(itemDto.getDescription() == null || itemDto.getDescription().isBlank())) {
            item.setDescription(itemDto.getDescription());
        }

        if (!(itemDto.getAvailable() == null)) {
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
