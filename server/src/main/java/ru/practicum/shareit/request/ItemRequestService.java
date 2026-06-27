package ru.practicum.shareit.request;

import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.common.request.ItemRequestDtoResponse;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDtoResponse getRequestById(Integer userId, Integer requestId);

    List<ItemRequestDtoResponse> getUserRequests(Integer userId);

    List<ItemRequestDtoResponse> getAllRequests(Integer userId);

    ItemRequestDtoResponse createRequest(Integer userId, ItemRequestDto itemRequestDto);
}