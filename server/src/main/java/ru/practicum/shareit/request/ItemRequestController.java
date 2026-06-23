package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.request.ItemRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/internal/requests")
public class ItemRequestController {

    private final ItemRequestService requestService;

    @GetMapping("/{requestId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                         @PathVariable Integer requestId) {
        log.info("Server: GET /requests/{}, userId={}", requestId, userId);
        return requestService.getRequestById(userId, requestId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Server: GET /requests, userId={}", userId);
        return requestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestDto> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Server: GET /requests/all, userId={}, from={}, size={}", userId, from, size);
        return requestService.getAllRequests(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                        @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Server: POST /requests, userId={}, itemRequestDto={}", userId, itemRequestDto);
        return requestService.createRequest(userId, itemRequestDto);
    }
}