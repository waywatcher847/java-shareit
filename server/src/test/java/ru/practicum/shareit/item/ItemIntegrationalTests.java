package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.common.comment.CommentDtoRequest;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.item.ItemDtoOwner;
import ru.practicum.common.item.ItemDtoRequest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemIntegrationalTests {
    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void create_ValidItem_ReturnsSavedItem() {
        User owner = createUser("Owner", "owner@test.com");
        ItemDtoRequest request = ItemDtoRequest.builder()
                .name("DDD")
                .description("Powerful DDD")
                .available(true)
                .build();

        ItemDto result = itemService.create(request, owner.getId());

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("DDD");
        assertThat(result.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void create_WithRequestId_ReturnsSavedItemWithRequest() {
        User owner = createUser("Owner", "owner@test.com");
        ItemRequest itemRequest = createRequest("zxczxczxc", owner);

        ItemDtoRequest request = ItemDtoRequest.builder()
                .name("DDD")
                .description("Powerful DDD")
                .available(true)
                .requestId(itemRequest.getId())
                .build();

        ItemDto result = itemService.create(request, owner.getId());

        assertThat(result.getId()).isNotNull();
    }

    @Test
    void create_WithInvalidRequestId_ThrowsNotFoundException() {
        User owner = createUser("Owner", "owner@test.com");
        ItemDtoRequest request = ItemDtoRequest.builder()
                .name("DDD").description("Desc").available(true).requestId(999).build();

        assertThrows(NotFoundException.class, () -> itemService.create(request, owner.getId()));
    }

    @Test
    void create_WithInvalidUserId_ThrowsNotFoundException() {
        ItemDtoRequest request = ItemDtoRequest.builder()
                .name("DDD").description("Desc").available(true).build();

        assertThrows(NotFoundException.class, () -> itemService.create(request, 999));
    }

    @Test
    void update_ValidFields_UpdatesItem() {
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("OldName", "OldDesc", true, owner);
        ItemDtoRequest updateDto = ItemDtoRequest.builder()
                .name("NewName").description("NewDesc").available(false).build();

        ItemDto result = itemService.update(updateDto, item.getId(), owner.getId());

        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getDescription()).isEqualTo("NewDesc");
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void update_PartialUpdate_UpdatesOnlyProvidedFields() {
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("OldName", "OldDesc", true, owner);
        ItemDtoRequest updateDto = ItemDtoRequest.builder().name("NewName").build();

        ItemDto result = itemService.update(updateDto, item.getId(), owner.getId());

        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getDescription()).isEqualTo("OldDesc");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void update_NotOwner_ThrowsValidationException() {
        User owner = createUser("Owner", "owner@test.com");
        User other = createUser("Other", "other@test.com");
        Item item = createItem("Name", "Desc", true, owner);
        ItemDtoRequest updateDto = ItemDtoRequest.builder().name("NewName").build();

        assertThrows(ValidationException.class, () -> itemService.update(updateDto, item.getId(), other.getId()));
    }

    @Test
    void getById_Owner_ReturnsWithBookings() {
        User owner = createUser("Owner", "owner@test.com");
        User booker = createUser("Booker", "booker@test.com");
        Item item = createItem("Item", "Desc", true, owner);

        createBooking(item, booker, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), BookingStatus.APPROVED);
        createBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.APPROVED);

        ItemDto result = itemService.getById(item.getId(), owner.getId());

        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
    }

    @Test
    void getById_NotOwner_ReturnsWithoutBookings() {
        User owner = createUser("Owner", "owner@test.com");
        User other = createUser("Other", "other@test.com");
        Item item = createItem("Item", "Desc", true, owner);

        ItemDto result = itemService.getById(item.getId(), other.getId());

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getUserItems_HasItems_ReturnsListWithComments() {
        User owner = createUser("Owner", "owner@test.com");
        User author = createUser("Author", "author@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        createComment("!!!!!!", author, item);

        List<ItemDtoOwner> result = itemService.getUserItems(owner.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getComments()).hasSize(1);
        assertThat(result.getFirst().getComments().getFirst().getText()).isEqualTo("!!!!!!");
    }

    @Test
    void getByText_ValidText_ReturnsMatches() {
        User owner = createUser("Owner", "owner@test.com");
        createItem("Power DDD", "Desc", true, owner);
        createItem("Hammer", "Desc", true, owner);

        List<ItemDto> result = itemService.getByText("DDD", owner.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Power DDD");
    }

    @Test
    void getByText_EmptyText_ReturnsEmptyList() {
        List<ItemDto> result = itemService.getByText("   ", 1);
        assertThat(result).isEmpty();
    }

    @Test
    void deleteItem_Owner_DeletesItem() {
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);

        itemService.deleteItem(item.getId(), owner.getId());

        assertThat(itemRepository.findById(item.getId())).isEmpty();
    }

    @Test
    void deleteItem_NotOwner_ThrowsValidationException() {
        User owner = createUser("Owner", "owner@test.com");
        User other = createUser("Other", "other@test.com");
        Item item = createItem("Item", "Desc", true, owner);

        assertThrows(ValidationException.class, () -> itemService.deleteItem(item.getId(), other.getId()));
    }

    @Test
    void addComment_HasPastApprovedBooking_SavesComment() {
        User owner = createUser("Owner", "owner@test.com");
        User booker = createUser("Booker", "booker@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        createBooking(item, booker, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), BookingStatus.APPROVED);

        CommentDtoRequest commentDto = CommentDtoRequest.builder().text("Awesome!").build();

        var result = itemService.addComment(commentDto, booker.getId(), item.getId());

        assertThat(result.getText()).isEqualTo("Awesome!");
        assertThat(result.getAuthorName()).isEqualTo("Booker");
    }

    @Test
    void addComment_NoBooking_ThrowsValidationException() {
        User owner = createUser("Owner", "owner@test.com");
        User booker = createUser("Booker", "booker@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        CommentDtoRequest commentDto = CommentDtoRequest.builder().text("Awesome!").build();

        assertThrows(ValidationException.class, () -> itemService.addComment(commentDto, booker.getId(), item.getId()));
    }


    private User createUser(String name, String email) {
        return userRepository.save(User.builder().name(name).email(email).build());
    }

    private ItemRequest createRequest(String description, User requestor) {
        return itemRequestRepository.save(ItemRequest.builder()
                .description(description)
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build());
    }

    private Item createItem(String name, String description, Boolean available, User owner) {
        return itemRepository.save(Item.builder()
                .name(name)
                .description(description)
                .available(available)
                .owner(owner)
                .build());
    }

    private Booking createBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        return bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .status(status)
                .build());
    }

    private Comment createComment(String text, User author, Item item) {
        return commentRepository.save(Comment.builder()
                .text(text)
                .author(author)
                .item(item)
                .created(LocalDateTime.now())
                .build());
    }
}