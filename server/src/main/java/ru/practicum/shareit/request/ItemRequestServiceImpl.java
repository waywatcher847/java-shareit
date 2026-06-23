package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.item.ItemResponseDto;
import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    public ItemRequestDto getRequestById(Integer userId, Integer requestId) {
        log.info("ItemRequestServiceImpl->getRequestById start");
        log.info("userId={}, requestId={}", userId, requestId);

        log.info("Fetching item request with id={}", requestId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found!"));

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        List<Item> relatedItems = itemRepository.findByRequestId(requestId);
        log.info("Found {} items related to request id={}", relatedItems.size(), requestId);

        List<ItemResponseDto> itemResponseDtos = getItemResponses(relatedItems);
        ItemRequestDto result = itemRequestMapper.toItemRequestDtoWithItems(request, itemResponseDtos);
        log.info("Received item request: {}", result);

        log.info("ItemRequestServiceImpl->getRequestById end");
        return result;
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Integer userId) {
        log.info("ItemRequestServiceImpl->getUserRequests start");
        log.info("userId={}", userId);

        log.info("Fetching all requests for user with id={}", userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        log.info("Found {} requests for user with id={}", requests.size(), userId);

        List<ItemRequestDto> result = requests.stream()
                .map(request -> {
                    List<ItemResponseDto> answers = getItemResponses(itemRepository.findByRequestId(request.getId()));
                    ItemRequestDto requestDto = itemRequestMapper.toItemRequestDtoWithItems(request, answers);
                    log.info("Processed request with id={}", request.getId());
                    return requestDto;
                })
                .collect(Collectors.toList());

        log.info("ItemRequestServiceImpl->getUserRequests end");
        return result;
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Integer userId, Integer from, Integer size) {
        log.info("ItemRequestServiceImpl->getAllRequests start");
        log.info("userId={}, from={}, size={}", userId, from, size);

        log.info("Fetching all requests except for user with id={}, from {} size {}", userId, from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(userId, pageable);
        log.info("Found {} requests", requests.size());

        List<ItemRequestDto> result = requests.stream()
                .map(request -> {
                    List<ItemResponseDto> answers = itemRepository.findByRequestId(request.getId())
                            .stream()
                            .map(item -> new ItemResponseDto(
                                    item.getId(),
                                    item.getName(),
                                    item.getUserId()
                            ))
                            .collect(Collectors.toList());
                    ItemRequestDto requestDto = itemRequestMapper.toItemRequestDtoWithItems(request, answers);
                    log.info("Processed request with id={}", request.getId());
                    return requestDto;
                })
                .collect(Collectors.toList());

        log.info("ItemRequestServiceImpl->getAllRequests end");
        return result;
    }

    @Override
    @Transactional
    public ItemRequestDto createRequest(Integer userId, ItemRequestDto itemRequestDto) {
        log.info("ItemRequestServiceImpl->createRequest start");
        log.info("userId={}, itemRequestDto={}", userId, itemRequestDto);

        log.info("Creating new item request from user with id={}", userId);
        ItemRequest request = itemRequestMapper.toItemRequest(itemRequestDto);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found!"));

        request.setUserId(owner.getId());
        request.setCreated(LocalDateTime.now());
        ItemRequest savedRequest = itemRequestRepository.save(request);
        log.info("Created item request with id={}", savedRequest.getId());

        ItemRequestDto result = itemRequestMapper.toItemRequestDtoWithItems(savedRequest, List.of());

        log.info("ItemRequestServiceImpl->createRequest end");
        return result;
    }

    private List<ItemResponseDto> getItemResponses(List<Item> relatedItems) {
        log.info("Forming list of responses for {} items", relatedItems.size());
        return relatedItems.stream()
                .map(item -> {
                    ItemResponseDto itemResponseDto = new ItemResponseDto(
                            item.getId(),
                            item.getName(),
                            item.getUserId()
                    );
                    log.debug("Created response for item with id={}", item.getId());
                    return itemResponseDto;
                })
                .collect(Collectors.toList());
    }
}