package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoNew;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Slf4j
@Qualifier("ItemServiceInMemory")
public class ItemServiceInMemory implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceInMemory(@Qualifier("ItemStorageInMemory") ItemStorage itemStorage,
                               @Qualifier("UserStorageInMemory") UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto getByIdItem(Integer itemId) {
        log.info("ItemServiceInMemory->getByIdItem start");
        Item item = itemStorage.getById(itemId);
        log.info("ItemServiceInMemory->getByIdItem end");
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto createItem(ItemDtoNew newItemDto, Integer userId) {
        log.info("ItemServiceInMemory->createItem start");
        User user = validateUser(userId);
        Item item = ItemMapper.mapToItem(newItemDto);
        item.setOwner(user);
        Item itemResult = itemStorage.create(item);
        log.info("ItemServiceInMemory->createItem end");
        return ItemMapper.mapToItemDto(itemResult);
    }

    @Override
    public ItemDto updateItem(ItemDtoUpdate updateItemDto, Integer userId, Integer itemId) {
        log.info("ItemServiceInMemory->updateItem start");
        User user = validateUser(userId);
        Item item = validItem(itemId);
        Item itemUpdate = ItemMapper.updateItemField(item, updateItemDto);
        Item itemResult = itemStorage.update(itemUpdate);
        log.info("ItemServiceInMemory->updateItem end");
        return ItemMapper.mapToItemDto(itemResult);
    }

    @Override
    public List<ItemDto> getUserItems(Integer userId) {
        log.info("ItemServiceInMemory->getUserItems start");
        User user = validateUser(userId);
        List<ItemDto> itemList = itemStorage.getAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getOwner().equals(user))
                .map(ItemMapper::mapToItemDto)
                .toList();
        log.info("ItemServiceInMemory->getUserItems end");
        return itemList;
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        log.info("ItemServiceInMemory->searchItem start");
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        String searchText = text.trim().toLowerCase(Locale.ROOT);

        List<ItemDto> itemList = itemStorage.getAll().stream()
                .filter(Objects::nonNull).filter(Item::getAvailable)
                .filter(item -> item.getName()
                        .toLowerCase(Locale.ROOT)
                        .contains(searchText) || item.getDescription()
                        .toLowerCase(Locale.ROOT)
                        .contains(searchText))
                .map(ItemMapper::mapToItemDto)
                .toList();
        log.info("ItemServiceInMemory->searchItem end");
        return itemList;
    }

    @Override
    public void deleteItem(Integer itemId, Integer userId) {
        log.info("ItemServiceInMemory->deleteItem start");
        validateUser(userId);
        Item item = validItem(itemId);
        itemStorage.delete(itemId);
        log.info("ItemServiceInMemory->deleteItem end");
    }

    private User validateUser(Integer userId) {
        User user = userStorage.getById(userId);
        if (user == null) {
            throw new NotFoundException("User not found: " + userId);
        }
        return user;
    }

    private Item validItem(Integer itemId) {
        Item item = itemStorage.getById(itemId);
        if (item == null) {
            throw new NotFoundException("Item nor found: " + itemId);
        }
        return item;
    }
}
