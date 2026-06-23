package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;

import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.common.item.ItemDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

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
        log.info("Request getItemById ID={}", itemId);
        return get("/" + itemId);
    }

    public ResponseEntity<Object> getItemByUserId(Integer userId) {
        log.info("Request getItemByUserId ID={}", userId);
        return get("", userId);
    }

    public ResponseEntity<Object> searchText(String text) {
        log.info("Request searchText: {}", text);
        if (text == null || text.isBlank()) {
            return get("/search");
        }
        return get("/search?text=" + text);
    }

    public ResponseEntity<Object> create(Integer userId, ItemDto itemDto) {
        log.info("Request create itemId={}, data: {}", userId, itemDto);
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> update(Integer itemId, Integer userId, Map<String, Object> updates) {
        log.info("Request update itemId={} userId={}, data: {}", itemId, userId, updates);
        return patch("/" + itemId, userId, updates);
    }

    public ResponseEntity<Object> addComment(Integer itemId, Integer userId, CommentRequestDto commentDto) {

        log.info("Request addComment itemId={} userId=={}, text: {}",
                itemId, userId, commentDto.getText());
        return post("/" + itemId + "/comment", userId, commentDto);

    }

    public ResponseEntity<Object> deleteItem(Integer itemId) {
        return delete("/" + itemId);
    }

    public ResponseEntity<Object> getItemDtoWithBookingsAndComments(Integer itemId, Integer userId) {
        log.info("Request с ID={} userId={}", itemId, userId);

        return getWithHeaders("/" + itemId, userId);
    }

}