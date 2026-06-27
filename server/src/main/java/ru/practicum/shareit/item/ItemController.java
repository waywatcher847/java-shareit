package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.Constants;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentDtoRequest;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.item.ItemDtoRequest;
import ru.practicum.common.item.ItemDtoOwner;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(@RequestBody ItemDtoRequest itemDto,
                                          @RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("POST /items, userId={}, itemDto={}", userId, itemDto);
        ItemDto createdItem = itemService.create(itemDto, userId);
        return ResponseEntity
                .created(URI.create("/items/" + createdItem.getId()))
                .body(createdItem);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> update(@PathVariable Integer id,
                                          @RequestBody ItemDtoRequest itemDto,
                                          @RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("PATCH /items/{}, userId={}, itemDto={}", id, userId, itemDto);
        ItemDto updatedItem = itemService.update(itemDto, id, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Integer id,
                                               @RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("GET /items/{}, userId={}", id, userId);
        ItemDto item = itemService.getById(id, userId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemDtoOwner>> getAllItemsByUser(@RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("GET /items, userId={}", userId);
        List<ItemDtoOwner> items = itemService.getUserItems(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItem(@RequestParam String text,
                                                    @RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("GET /items/search, text='{}', userId={}", text, userId);
        List<ItemDto> items = itemService.getByText(text, userId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Integer itemId,
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @RequestBody @Valid CommentDtoRequest commentDtoRequest) {
        log.info("POST /items/{}/comment, userId={}, comment={}", itemId, userId, commentDtoRequest);
        CommentDto comment = itemService.addComment(commentDtoRequest, userId, itemId);
        return ResponseEntity.ok(comment);
    }
}