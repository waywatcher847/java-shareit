package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.request.ItemRequestDto;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @GetMapping("/{requestId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                 @PathVariable Integer requestId) {
        log.info("Gateway: GET /requests/{}", requestId);
        return itemRequestClient.getRequestById(userId, requestId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Gateway: GET /requests");
        return itemRequestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Gateway: GET /requests/all");
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createRequest(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info("Gateway: POST /requests");
        return itemRequestClient.createRequest(userId, itemRequestDto);
    }
}