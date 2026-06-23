package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.common.item.ItemDto;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(@Valid @RequestBody ItemDto itemDto,
                                          @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Server: POST /items, userId={}, itemDto={}", userId, itemDto);
        ItemDto createdItem = itemService.create(itemDto, userId);
        return ResponseEntity
                .created(URI.create("/items/" + createdItem.getId()))
                .body(createdItem);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> update(@PathVariable Integer id,
                                          @Valid @RequestBody ItemDto itemDto,
                                          @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Server: PATCH /items/{}, userId={}, itemDto={}", id, userId, itemDto);
        ItemDto updatedItem = itemService.update(id, itemDto, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Integer id,
                                               @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Server: GET /items/{}, userId={}", id, userId);
        ItemDto item = itemService.getItemByIdWithDetails(id, userId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItemsByUser(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Server: GET /items, userId={}", userId);
        List<ItemDto> items = itemService.getAllItemsByUser(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItem(@RequestParam String text,
                                                    @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Server: GET /items/search, text='{}', userId={}", text, userId);
        List<ItemDto> items = itemService.searchItem(text, userId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Integer itemId,
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @RequestBody CommentRequestDto commentRequestDto) {
        log.info("Server: POST /items/{}/comment, userId={}, comment={}", itemId, userId, commentRequestDto);
        CommentDto comment = itemService.addComment(userId, itemId, commentRequestDto);
        return ResponseEntity.ok(comment);
    }
}