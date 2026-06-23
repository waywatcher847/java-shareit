package ru.practicum.shareit.item;


import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.common.item.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Integer userId);

    ItemDto update(Integer id, ItemDto itemDto, Integer userId);

    ItemDto getItemById(Integer id);

    ItemDto getItemByIdWithDetails(Integer id, Integer userId);

    List<ItemDto> getAllItemsByUser(Integer userId);

    List<ItemDto> searchItem(String text, Integer userId);

    CommentDto addComment(Integer userId, Integer itemId, CommentRequestDto commentRequestDto);
}