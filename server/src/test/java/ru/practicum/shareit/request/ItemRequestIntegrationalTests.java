package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestIntegrationalTests {

    private final ItemRequestService itemRequestService;
    private final UserService userService;

    @Test
    void createRequest_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        Integer nonExistentUserId = 99;

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("I need something");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.createRequest(nonExistentUserId, requestDto));

        assertEquals("User not found!", exception.getMessage());
    }

    @Test
    void createRequest_WhenUserExists_ShouldCreateRequest() {
        UserDto userDto = UserDto.builder()
                .name("User1")
                .email("user1@mail.ru")
                .build();
        UserDto createdUser = userService.create(userDto);
        Integer userId = createdUser.getId();

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("I need a drill for the weekend");

        ItemRequestDto createdRequest = itemRequestService.createRequest(userId, requestDto);
        Integer requestId = createdRequest.getId();

        ItemRequestDto retrievedRequest = itemRequestService.getRequestById(userId, requestId);

        assertThat(retrievedRequest.getId()).isEqualTo(requestId);
        assertThat(retrievedRequest.getDescription()).isEqualTo("I need a drill for the weekend");
        assertThat(retrievedRequest.getItems()).isNotNull().isEmpty();
    }

    @Test
    void getRequestById_WhenRequestDoesNotExist_ShouldThrowNotFoundException() {
        UserDto userDto = UserDto.builder().name("User2").email("user2@mail.ru").build();
        UserDto createdUser = userService.create(userDto);
        Integer userId = createdUser.getId();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(userId, 99));

        assertEquals("Request not found", exception.getMessage());
    }

    @Test
    void getRequestById_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        Integer nonExistentUserId = 99;
        Integer requestId = 1;

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(nonExistentUserId, requestId));

        assertEquals("User not found!", exception.getMessage());
    }

    @Test
    void getAllRequests_WhenFetchingAll_ShouldExcludeOwnRequests() {
        UserDto user1Dto = UserDto.builder().name("User4").email("user4@mail.ru").build();
        UserDto createdUser1 = userService.create(user1Dto);
        Integer user1Id = createdUser1.getId();

        UserDto user2Dto = UserDto.builder().name("User5").email("user5@mail.ru").build();
        UserDto createdUser2 = userService.create(user2Dto);
        Integer user2Id = createdUser2.getId();

        ItemRequestDto req1 = new ItemRequestDto();
        req1.setDescription("User1 Request");
        itemRequestService.createRequest(user1Id, req1);

        ItemRequestDto req2 = new ItemRequestDto();
        req2.setDescription("User2 Request");
        itemRequestService.createRequest(user2Id, req2);

        List<ItemRequestDto> requestsForUser1 = itemRequestService.getAllRequests(user1Id, 0, 10);
        assertThat(requestsForUser1).hasSize(1);
        assertThat(requestsForUser1.getFirst().getDescription()).isEqualTo("User2 Request");

        List<ItemRequestDto> requestsForUser2 = itemRequestService.getAllRequests(user2Id, 0, 10);
        assertThat(requestsForUser2).hasSize(1);
        assertThat(requestsForUser2.getFirst().getDescription()).isEqualTo("User1 Request");
    }

    @Test
    void getUserRequests_WhenUserHasRequests_ShouldReturnUserRequests() {
        UserDto userDto = UserDto.builder().name("User3").email("user3@mail.ru").build();
        UserDto createdUser = userService.create(userDto);
        Integer userId = createdUser.getId();

        ItemRequestDto req1 = new ItemRequestDto();
        req1.setDescription("Request 1");
        itemRequestService.createRequest(userId, req1);

        ItemRequestDto req2 = new ItemRequestDto();
        req2.setDescription("Request 2");
        itemRequestService.createRequest(userId, req2);

        List<ItemRequestDto> requests = itemRequestService.getUserRequests(userId);

        assertThat(requests).hasSize(2);
        assertThat(requests)
                .extracting(ItemRequestDto::getDescription)
                .containsExactlyInAnyOrder("Request 1", "Request 2");
    }
}