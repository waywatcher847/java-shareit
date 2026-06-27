package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.Constants;
import ru.practicum.common.request.ItemRequestDto;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @GetMapping("/{requestId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getRequestById(@RequestHeader(Constants.USER_ID_HEADER) Integer userId,
                                                 @PathVariable Integer requestId) {
        log.info(" GET /requests/{}", requestId);
        return itemRequestClient.getRequestById(userId, requestId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getUserRequests(@RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info(" GET /requests");
        return itemRequestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info(" GET /requests/all");
        return itemRequestClient.getAllRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createRequest(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info(" POST /requests");
        return itemRequestClient.createRequest(userId, itemRequestDto);
    }
}