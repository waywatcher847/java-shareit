package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.Constants;
import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.common.request.ItemRequestDtoResponse;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/internal/requests")
public class ItemRequestController {

    private final ItemRequestService requestService;

    @GetMapping("/{requestId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemRequestDtoResponse getRequestById(@RequestHeader(Constants.USER_ID_HEADER) Integer userId,
                                         @PathVariable Integer requestId) {
        log.info("Server: GET /requests/{}, userId={}", requestId, userId);
        return requestService.getRequestById(userId, requestId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestDtoResponse> getUserRequests(@RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("Server: GET /requests, userId={}", userId);
        return requestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestDtoResponse> getAllRequests(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("Server: GET /requests/all, userId={}", userId);
        return requestService.getAllRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDtoResponse createRequest(@RequestHeader(Constants.USER_ID_HEADER) Integer userId,
                                        @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info("Server: POST /requests, userId={}, itemRequestDto={}", userId, itemRequestDto);
        return requestService.createRequest(userId, itemRequestDto);
    }
}