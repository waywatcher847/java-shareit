package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoNew;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

import java.util.List;

public interface ItemService {

    ItemDto getByIdItem(Integer itemId);

    ItemDto createItem(ItemDtoNew newItemDto, Integer userId);

    ItemDto updateItem(ItemDtoUpdate updateItemDto, Integer userId, Integer itemId);

    List<ItemDto> getUserItems(Integer userId);

    List<ItemDto> searchItem(String text);

    void deleteItem(Integer itemId, Integer userId);
}
