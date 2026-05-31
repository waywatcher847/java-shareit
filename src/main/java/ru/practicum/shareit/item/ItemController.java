package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.constants.Constants;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoNew;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(@Qualifier("ItemServiceInMemory") ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{itemId}")
    public ItemDto getByIdItem(@PathVariable("itemId") Integer itemId) {
        log.info("Get /items/" + itemId);
        return itemService.getByIdItem(itemId);
    }

    @PostMapping
    public ItemDto createItem(@Valid @RequestBody ItemDtoNew newItemDto,
                              @RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("Post /items/" + userId);
        return itemService.createItem(newItemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@Valid @RequestBody ItemDtoUpdate updateItemDto,
                              @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
                              @PathVariable("itemId") Integer itemId) {
        log.info("Patch /items/" + itemId);
        return itemService.updateItem(updateItemDto, userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("Get /items/" + userId);
        return itemService.getUserItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam(value = "text", required = false) String text) {
        log.info("Get /items/search/" + text);
        return itemService.searchItem(text);
    }
}
