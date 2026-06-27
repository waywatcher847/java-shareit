package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.common.request.ItemRequestDtoResponse;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemRequestIntegrationalTests {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createRequest_UserExists_ReturnsSavedRequest() {
        User user = createUser("User", "user@test.com");
        ItemRequestDto dto = ItemRequestDto.builder().description("Need a tool").build();

        ItemRequestDtoResponse response = itemRequestService.createRequest(user.getId(), dto);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getDescription()).isEqualTo("Need a tool");
        assertThat(response.getCreated()).isNotNull();
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void createRequest_UserNotExists_ThrowsNotFoundException() {
        ItemRequestDto dto = ItemRequestDto.builder().description("Need a tool").build();

        assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(999, dto));
    }

    @Test
    void getRequestById_RequestExistsNoItems_ReturnsRequest() {
        User user = createUser("User", "user@test.com");
        ItemRequest request = createRequest("Desc", user, LocalDateTime.now());

        ItemRequestDtoResponse response = itemRequestService.getRequestById(user.getId(), request.getId());

        assertThat(response.getId()).isEqualTo(request.getId());
        assertThat(response.getDescription()).isEqualTo("Desc");
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void getRequestById_RequestExistsWithItems_ReturnsRequestWithItems() {
        User user = createUser("User", "user@test.com");
        User owner = createUser("Owner", "owner@test.com");
        ItemRequest request = createRequest("Desc", user, LocalDateTime.now());

        // Передаем сам объект request, а не его ID
        createItem("Item1", owner, request);
        createItem("Item2", owner, request);

        ItemRequestDtoResponse response = itemRequestService.getRequestById(user.getId(), request.getId());

        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems())
                .extracting("name")
                .containsExactlyInAnyOrder("Item1", "Item2");
    }

    @Test
    void getRequestById_RequestNotExists_ThrowsNotFoundException() {
        User user = createUser("User", "user@test.com");
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(user.getId(), 999));
    }

    @Test
    void getUserRequests_UserHasNoRequests_ReturnsEmptyList() {
        User user = createUser("User", "user@test.com");

        List<ItemRequestDtoResponse> responses = itemRequestService.getUserRequests(user.getId());

        assertThat(responses).isEmpty();
    }

    @Test
    void getUserRequests_UserHasRequests_ReturnsSortedRequests() {
        User user = createUser("User", "user@test.com");
        createRequest("Old", user, LocalDateTime.now().minusDays(1));
        createRequest("New", user, LocalDateTime.now());

        List<ItemRequestDtoResponse> responses = itemRequestService.getUserRequests(user.getId());

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getDescription()).isEqualTo("New");
        assertThat(responses.get(1).getDescription()).isEqualTo("Old");
    }

    @Test
    void getAllRequests_NoRequests_ReturnsEmptyList() {
        User user = createUser("User", "user@test.com");
        List<ItemRequestDtoResponse> responses = itemRequestService.getAllRequests(user.getId());
        assertThat(responses).isEmpty();
    }

    @Test
    void getAllRequests_ExcludesCurrentUserAndSorted() {
        User currentUser = createUser("Current", "current@test.com");
        User user1 = createUser("User1", "u1@test.com");
        User user2 = createUser("User2", "u2@test.com");

        createRequest("Old", user1, LocalDateTime.now().minusDays(2));
        createRequest("New", user2, LocalDateTime.now().minusDays(1));
        createRequest("Current Req", currentUser, LocalDateTime.now());

        List<ItemRequestDtoResponse> responses = itemRequestService.getAllRequests(currentUser.getId());

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getDescription()).isEqualTo("New");
        assertThat(responses.get(1).getDescription()).isEqualTo("Old");
    }

    @Test
    void getAllRequests_ChecksItemsOwnerMapping() {
        User currentUser = createUser("Current", "current@test.com");
        User requestor = createUser("Requestor", "requestor@test.com");
        User itemOwner = createUser("Owner", "owner@test.com");

        ItemRequest request = createRequest("Req", requestor, LocalDateTime.now());
        createItem("Item1", itemOwner, request);

        List<ItemRequestDtoResponse> responses = itemRequestService.getAllRequests(currentUser.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getItems()).hasSize(1);
        assertThat(responses.getFirst().getItems().getFirst().getUser().getEmail()).isEqualTo("owner@test.com");
    }

    private User createUser(String name, String email) {
        User user = User.builder().name(name).email(email).build();
        return userRepository.save(user);
    }

    private ItemRequest createRequest(String description, User requestor, LocalDateTime created) {
        ItemRequest request = ItemRequest.builder()
                .description(description)
                .requestor(requestor)
                .created(created)
                .build();
        return itemRequestRepository.save(request);
    }

    private Item createItem(String name, User owner, ItemRequest request) {
        Item item = Item.builder()
                .name(name)
                .description("Description for " + name)
                .available(true)
                .owner(owner)
                .request(request)
                .build();
        return itemRepository.save(item);
    }
}