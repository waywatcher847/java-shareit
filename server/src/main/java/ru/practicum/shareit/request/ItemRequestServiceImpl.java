package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.item.ItemDtoResponse;
import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.common.request.ItemRequestDtoResponse;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.validation.ValidationService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ValidationService validationService;

    @Override
    public ItemRequestDtoResponse getRequestById(Integer userId, Integer requestId) {
        log.info("ItemRequestServiceImpl->getRequestById start, userId={}, requestId={}", userId, requestId);

        ItemRequest request = validationService.validateItemRequestExists(requestId);

        List<Item> relatedItems = itemRepository.findByRequestId(requestId);

        List<ItemDtoResponse> itemDtoResponses = getItemResponses(relatedItems);
        ItemRequestDtoResponse result = itemRequestMapper.toItemRequestDtoWithItems(request, itemDtoResponses);

        log.info("ItemRequestServiceImpl->getRequestById end, result={}", result);
        return result;
    }

    @Override
    public List<ItemRequestDtoResponse> getUserRequests(Integer userId) {
        log.info("ItemRequestServiceImpl->getUserRequests start, userId={}", userId);

        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        log.info("Found {} requests", requests.size());

        List<ItemRequestDtoResponse> result;
        if (requests.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<Integer> requestIds = requests.stream()
                    .map(ItemRequest::getId)
                    .collect(Collectors.toList());

            List<Item> items = itemRepository.findByRequestIdInFetchOwner(requestIds);

            Map<Integer, List<Item>> itemsByRequest = items.stream()
                    .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

            result = requests.stream()
                    .map(request -> {
                        List<Item> relatedItems = itemsByRequest.getOrDefault(request.getId(), Collections.emptyList());
                        List<ItemDtoResponse> answers = getItemResponses(relatedItems);
                        ItemRequestDtoResponse requestDto = itemRequestMapper.toItemRequestDtoWithItems(request, answers);
                        log.info("Processed request with id={}", request.getId());
                        return requestDto;
                    })
                    .collect(Collectors.toList());
        }

        log.info("ItemRequestServiceImpl->getUserRequests end, result size={}", result.size());
        return result;
    }

    @Override
    public List<ItemRequestDtoResponse> getAllRequests(Integer userId) {
        log.info("ItemRequestServiceImpl->getAllRequests start, userId={}", userId);

        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(userId);
        log.info("Found {} requests", requests.size());

        List<ItemRequestDtoResponse> result;
        if (requests.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<Integer> requestIds = requests.stream()
                    .map(ItemRequest::getId)
                    .collect(Collectors.toList());

            List<Item> items = itemRepository.findByRequestIdInFetchOwner(requestIds);

            Map<Integer, List<Item>> itemsByRequest = items.stream()
                    .collect(Collectors.groupingBy(item -> item.getRequest().getId()));


            result = requests.stream()
                    .map(request -> {
                        List<Item> relatedItems = itemsByRequest.getOrDefault(request.getId(), Collections.emptyList());
                        List<ItemDtoResponse> answers = getItemResponses(relatedItems);
                        ItemRequestDtoResponse requestDto = itemRequestMapper.toItemRequestDtoWithItems(request, answers);
                        log.info("Processed request with id={}", request.getId());
                        return requestDto;
                    })
                    .collect(Collectors.toList());
        }

        log.info("ItemRequestServiceImpl->getAllRequests end, result size={}", result.size());
        return result;
    }

    @Override
    @Transactional
    public ItemRequestDtoResponse createRequest(Integer userId, ItemRequestDto itemRequestDto) {
        log.info("ItemRequestServiceImpl->createRequest start, userId={}, itemRequestDto={}", userId, itemRequestDto);

        ItemRequest request = itemRequestMapper.toItemRequest(itemRequestDto);
        User requestor = validationService.validateUserExists(userId);

        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(request);
        log.info("DEBUG: Created request_id={}, user_id={}", savedRequest.getId(), savedRequest.getRequestor().getId());


        List<Item> items = itemRepository.findByRequestId(savedRequest.getId());
        log.info("DEBUG: {} items", items.size());

        ItemRequestDtoResponse result = itemRequestMapper.toItemRequestDtoWithItems(savedRequest, List.of());

        log.info("ItemRequestServiceImpl->createRequest end, result={}", result);
        return result;
    }

    private List<ItemDtoResponse> getItemResponses(List<Item> relatedItems) {
        log.info("ItemRequestServiceImpl->getItemResponses start, items count={}", relatedItems.size());

        List<ItemDtoResponse> result = relatedItems.stream()
                .map(item -> {
                    ItemDtoResponse itemDtoResponse = new ItemDtoResponse(
                            item.getId(),
                            item.getName(),
                            UserMapper.mapToUserDto(item.getOwner())
                    );
                    log.debug("Created response for item with id={}", item.getId());
                    return itemDtoResponse;
                })
                .collect(Collectors.toList());
        log.info("ItemRequestServiceImpl->getItemResponses end, result size={}", result.size());
        return result;
    }
}