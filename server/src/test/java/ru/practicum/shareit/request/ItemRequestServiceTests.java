package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.common.item.ItemResponseDto;
import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTests {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Mock
    private User user;
    @Mock
    private ItemRequest itemRequestEntity;
    @Mock
    private Item itemEntity;
    @Mock
    private ItemRequestDto itemRequestDto;
    @Mock
    private ItemResponseDto itemResponseDto;

    private static final Integer TEST_USER = 1;
    private static final Integer TEST_REQUEST = 10;
    private static final Integer TEST_ITEM = 100;


    @Test
    void getRequestById_WhenRequestExists_ReturnsRequestDto() {
        when(userRepository.findById(TEST_USER)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(TEST_REQUEST)).thenReturn(Optional.of(itemRequestEntity));
        when(itemRepository.findByRequestId(TEST_REQUEST)).thenReturn(List.of(itemEntity));
        when(itemEntity.getId()).thenReturn(TEST_ITEM);
        when(itemEntity.getName()).thenReturn("Test Item");
        when(itemEntity.getUserId()).thenReturn(TEST_USER);
        when(itemRequestMapper.toItemRequestDtoWithItems(eq(itemRequestEntity), anyList()))
                .thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.getRequestById(TEST_USER, TEST_REQUEST);

        assertNotNull(result);
        assertEquals(itemRequestDto, result);
        verify(userRepository).findById(TEST_USER);
        verify(itemRequestRepository).findById(TEST_REQUEST);
        verify(itemRepository).findByRequestId(TEST_REQUEST);
    }

    @Test
    void getUserRequests_WhenRequestsExist_ReturnsRequestsList() {
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(TEST_USER))
                .thenReturn(List.of(itemRequestEntity));
        when(itemRequestEntity.getId()).thenReturn(TEST_REQUEST);
        when(itemRepository.findByRequestId(TEST_REQUEST)).thenReturn(List.of(itemEntity));
        when(itemEntity.getId()).thenReturn(TEST_ITEM);
        when(itemEntity.getName()).thenReturn("Test Item");
        when(itemEntity.getUserId()).thenReturn(TEST_USER);
        when(itemRequestMapper.toItemRequestDtoWithItems(eq(itemRequestEntity), anyList()))
                .thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.getUserRequests(TEST_USER);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemRequestDto, result.getFirst());
    }

    @Test
    void getRequestById_WhenUserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(TEST_USER)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getRequestById(TEST_USER, TEST_REQUEST)
        );
    }

    @Test
    void getRequestById_WhenRequestNotFound_ThrowsRuntimeException() {
        when(userRepository.findById(TEST_USER)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(TEST_REQUEST)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                itemRequestService.getRequestById(TEST_USER, TEST_REQUEST)
        );
        assertEquals("Request not found", exception.getMessage());
    }

    @Test
    void getAllRequests_WhenRequestsExist_ReturnsRequestsList() {
        int from = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(from, size);

        when(itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(TEST_USER, pageable))
                .thenReturn(List.of(itemRequestEntity));
        when(itemRequestEntity.getId()).thenReturn(TEST_REQUEST);
        when(itemRepository.findByRequestId(TEST_REQUEST)).thenReturn(List.of(itemEntity));
        when(itemEntity.getId()).thenReturn(TEST_ITEM);
        when(itemEntity.getName()).thenReturn("Test Item");
        when(itemEntity.getUserId()).thenReturn(TEST_USER);
        when(itemRequestMapper.toItemRequestDtoWithItems(eq(itemRequestEntity), anyList()))
                .thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(TEST_USER, from, size);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemRequestDto, result.getFirst());
    }


    @Test
    void getUserRequests_WhenNoRequestsExist_ReturnsEmptyList() {
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(TEST_USER))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.getUserRequests(TEST_USER);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createRequest_WhenUserNotFound_ThrowsNotFoundException() {
        when(itemRequestMapper.toItemRequest(itemRequestDto)).thenReturn(itemRequestEntity);
        when(userRepository.findById(TEST_USER)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.createRequest(TEST_USER, itemRequestDto)
        );

        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void createRequest_WhenValidData_ReturnsCreatedRequest() {
        LocalDateTime now = LocalDateTime.now();
        ItemRequest savedRequest = mock(ItemRequest.class);

        when(itemRequestMapper.toItemRequest(itemRequestDto)).thenReturn(itemRequestEntity);
        when(userRepository.findById(TEST_USER)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(TEST_USER);
        when(itemRequestRepository.save(itemRequestEntity)).thenReturn(savedRequest);
        when(savedRequest.getId()).thenReturn(TEST_REQUEST);
        when(itemRequestMapper.toItemRequestDtoWithItems(eq(savedRequest), eq(List.of())))
                .thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.createRequest(TEST_USER, itemRequestDto);

        assertNotNull(result);
        assertEquals(itemRequestDto, result);

        verify(itemRequestEntity).setUserId(TEST_USER);
        verify(itemRequestEntity).setCreated(any(LocalDateTime.class));
        verify(itemRequestRepository).save(itemRequestEntity);
    }
}