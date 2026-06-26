package ru.practicum.shareit.item;


import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentDtoRequest;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.item.ItemDtoRequest;
import ru.practicum.common.item.ItemDtoOwner;

import java.util.List;

public interface ItemService {

    ItemDto getById(Integer itemId, Integer userId);

    ItemDto create(ItemDtoRequest newItemDto, Integer userId);

    ItemDto update(ItemDtoRequest updateItemDto, Integer itemId, Integer userId);

    List<ItemDtoOwner> getUserItems(Integer userId);

    List<ItemDto> getByText(String text, Integer userId);

    void deleteItem(Integer itemId, Integer userId);

    CommentDto addComment(CommentDtoRequest commentDto, Integer userId, Integer itemId);

}
