package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.shareit.client.BaseClient;

@Slf4j
@Service
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/internal/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getRequestById(Integer userId, Integer requestId) {
        log.info("getRequestById userId={}, requestId: {}", userId, requestId);
        return get("/" + requestId, userId);
    }

    public ResponseEntity<Object> getUserRequests(Integer userId) {
        log.info("getUserRequests userId={}", userId);
        return get("", userId);
    }

    public ResponseEntity<Object> getAllRequests(Integer userId) {
        log.info("getAllRequests userId={}", userId);
        return get("/all", userId);
    }

    public ResponseEntity<Object> createRequest(Integer userId, @Valid ItemRequestDto itemRequestDto) {
        log.info("createRequest userId={}, data: {}", userId, itemRequestDto);
        return post("", userId, itemRequestDto);
    }
}