package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;

import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.common.comment.CommentDtoRequest;
import ru.practicum.common.item.ItemDtoRequest;
import ru.practicum.shareit.client.BaseClient;

@Slf4j
@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/internal/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getItemById(Integer itemId) {
        log.info("getItemById ID={}", itemId);
        return get("/" + itemId);
    }

    public ResponseEntity<Object> getItemByUserId(Integer userId) {
        log.info("getItemByUserId ID={}", userId);
        return get("", userId);
    }

    public ResponseEntity<Object> searchText(String text, Integer userId) {
        log.info("searchText: {}", text);
        return get("/search?text=" + text, userId);
    }

    public ResponseEntity<Object> create(Integer userId, ItemDtoRequest itemDto) {
        log.info("create userId={}, data: {}", userId, itemDto);
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> update(Integer itemId, Integer userId, ItemDtoRequest itemDto) {
        log.info("update itemId={} userId={}, data: {}", itemId, userId, itemDto);
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> addComment(Integer itemId, Integer userId, CommentDtoRequest commentDto) {
        log.info("addComment itemId={} userId=={}, text: {}",
                itemId, userId, commentDto.getText());
        return post("/" + itemId + "/comment", userId, commentDto);

    }

    public ResponseEntity<Object> deleteItem(Integer itemId) {
        return delete("/" + itemId);
    }

    public ResponseEntity<Object> getItemDtoWithBookingsAndComments(Integer itemId, Integer userId) {
        log.info("с ID={} userId={}", itemId, userId);

        return getWithHeaders("/" + itemId, userId);
    }

}