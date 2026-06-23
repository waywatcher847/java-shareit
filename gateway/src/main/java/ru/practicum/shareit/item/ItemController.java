package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.common.item.ItemDto;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getItemById(@PathVariable Integer itemId,
                                              @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Gateway: GET /items/{}", itemId);
        return itemClient.getItemDtoWithBookingsAndComments(itemId, userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getItemByUserId(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Gateway: GET /items");
        return itemClient.getItemByUserId(userId);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> searchText(@RequestParam("text") @NotBlank String text) {
        log.info("Gateway: GET /items/search");
        return itemClient.searchText(text);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                         @RequestBody @Valid ItemDto itemDto) {
        log.info("Gateway: POST /items");
        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@PathVariable Integer itemId,
                                         @RequestHeader("X-Sharer-User-Id") Integer userId,
                                         @RequestBody Map<String, Object> updates) {
        log.info("Gateway: PATCH /items/{}", itemId);
        return itemClient.update(itemId, userId, updates);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> addComment(@PathVariable Integer itemId,
                                             @RequestHeader("X-Sharer-User-Id") Integer userId,
                                             @Valid @RequestBody CommentRequestDto commentDto) {
        log.info("Gateway: POST /items/{}/comment", itemId);
        return itemClient.addComment(itemId, userId, commentDto);
    }
}