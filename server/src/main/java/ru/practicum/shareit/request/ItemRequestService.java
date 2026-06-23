package ru.practicum.shareit.request;

import ru.practicum.common.request.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto getRequestById(Integer userId, Integer requestId);

    List<ItemRequestDto> getUserRequests(Integer userId);

    List<ItemRequestDto> getAllRequests(Integer userId, Integer from, Integer size);

    ItemRequestDto createRequest(Integer userId, ItemRequestDto itemRequestDto);
}