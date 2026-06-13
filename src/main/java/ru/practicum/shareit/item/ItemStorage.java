package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    List<Item> getAll();

    Item getById(Integer itemId);

    Item create(Item item);

    Item update(Item item);

    void delete(Integer itemId);
}
