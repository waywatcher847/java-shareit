package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.Constants;
import ru.practicum.common.comment.CommentDtoRequest;
import ru.practicum.common.item.ItemDtoRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getItemById(@PathVariable Integer itemId,
                                              @RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("GET /items/{}", itemId);
        return itemClient.getItemDtoWithBookingsAndComments(itemId, userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getItemByUserId(@RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("GET /items");
        return itemClient.getItemByUserId(userId);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> searchText(@RequestParam("text") String text,
                                             @RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("GET /items/search");
        return itemClient.searchText(text, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader(Constants.USER_ID_HEADER) Integer userId,
                                         @RequestBody @Validated(ItemDtoRequest.Create.class) ItemDtoRequest itemDto) {
        log.info("POST /items");
        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@PathVariable Integer itemId,
                                         @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
                                         @RequestBody ItemDtoRequest itemDto) {
        log.info("PATCH /items/{}", itemId);
        return itemClient.update(itemId, userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> addComment(@PathVariable Integer itemId,
                                             @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
                                             @RequestBody @Valid CommentDtoRequest commentDto) {
        log.info("POST /items/{}/comment", itemId);
        return itemClient.addComment(itemId, userId, commentDto);
    }
}