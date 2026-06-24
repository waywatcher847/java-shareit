package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.common.item.ItemDto;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTests {

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;


    private User createUser() {
        return User.builder()
                .id(1)
                .name("User1")
                .email("user1@gmail.com")
                .build();
    }

    private Item buildItem() {
        return Item.builder()
                .id(1)
                .name("Item1")
                .description("Desc1")
                .available(true)
                .userId(1)
                .build();
    }

    private ItemDto createItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item1");
        itemDto.setDescription("Desc1");
        itemDto.setAvailable(true);
        return itemDto;
    }

    private CommentDto createCommentDto() {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1);
        commentDto.setText("Comment1");
        commentDto.setItemId(1);
        commentDto.setUserId(1);
        commentDto.setAuthorName("User1");
        commentDto.setCreated(Instant.now());
        return commentDto;
    }

    private Comment createComment() {
        return Comment.builder()
                .id(1)
                .text("Comment1")
                .itemId(1)
                .userId(1)
                .created(Instant.now())
                .build();
    }

    @Test
    void createItem_WhenUserExists_ShouldReturnCreatedItem() {
        Integer userId = 1;
        ItemDto request = createItemDto();

        User user = createUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Item savedItem = buildItem();
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemDto expectedItem = createItemDto();
        expectedItem.setId(1);
        expectedItem.setUserId(1);
        expectedItem.setComments(Collections.emptyList());

        when(itemMapper.toEntity(request)).thenReturn(savedItem);
        when(itemMapper.toDto(savedItem)).thenReturn(expectedItem);

        ItemDto result = itemService.create(request, userId);

        assertThat(result, equalTo(expectedItem));
    }

    @Test
    void updateItem_WhenCalledByOwner_ShouldReturnUpdatedItem() {
        Integer userId = 1;
        Integer itemId = 1;
        ItemDto request = createItemDto();

        Item editingItem = buildItem();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(editingItem));

        Item savedItem = buildItem();
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemDto expectedItem = createItemDto();
        expectedItem.setId(1);
        expectedItem.setUserId(1);
        expectedItem.setComments(List.of(createCommentDto()));

        when(itemMapper.toDto(savedItem)).thenReturn(expectedItem);
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(createComment()));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(createCommentDto());
        when(userRepository.findById(1)).thenReturn(Optional.of(createUser()));

        ItemDto result = itemService.update(itemId, request, userId);

        assertThat(result, equalTo(expectedItem));
    }

    @Test
    void createItem_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        Integer userId = 999;
        ItemDto request = createItemDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(request, userId));

        assertEquals("owner not found for id = " + userId, exception.getMessage());
    }

    @Test
    void updateItem_WhenItemDoesNotExist_ShouldThrowNotFoundException() {
        Integer userId = 1;
        Integer itemId = 5;
        ItemDto request = createItemDto();

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(itemId, request, userId));

        assertEquals("Item with id = " + itemId + " not found", exception.getMessage());
    }

    @Test
    void updateItem_WhenCalledByNotOwner_ShouldThrowNotFoundException() {
        Integer userId = 2;
        Integer itemId = 1;
        ItemDto request = createItemDto();

        Item editingItem = buildItem();
        editingItem.setUserId(1);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(editingItem));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(itemId, request, userId));

        assertEquals("Owner id =/= provided id", exception.getMessage());
    }

    @Test
    void getItemByIdWithDetails_WhenItemDoesNotExist_ShouldThrowNotFoundException() {
        Integer userId = 1;
        Integer itemId = 7;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getItemByIdWithDetails(itemId, userId));

        assertEquals("Item with id = " + itemId + " not found", exception.getMessage());
    }

    @Test
    void getItemByIdWithDetails_WhenItemExists_ShouldReturnItemWithDetails() {
        Integer userId = 1;
        Integer itemId = 1;

        Item item = buildItem();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        CommentDto commentDto = createCommentDto();
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(createComment()));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDto);
        when(userRepository.findById(1)).thenReturn(Optional.of(createUser()));

        ItemDto expectedItem = createItemDto();
        expectedItem.setId(1);
        expectedItem.setUserId(1);
        expectedItem.setComments(List.of(commentDto));

        when(bookingRepository.findLastBookingForItem(eq(itemId), eq(userId), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findNextBookingForItem(eq(itemId), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        when(itemMapper.toDto(eq(item), isNull(), isNull(), anyList())).thenReturn(expectedItem);

        ItemDto result = itemService.getItemByIdWithDetails(itemId, userId);

        assertThat(result, equalTo(expectedItem));
    }

    @Test
    void searchItem_WhenItemsMatch_ShouldReturnMatchingItems() {
        Integer userId = 1;
        String searchingSubstring = "substring";

        Item item = buildItem();
        List<Item> items = List.of(item);
        when(itemRepository.searchItem(searchingSubstring)).thenReturn(items);

        ItemDto expectedItem = createItemDto();
        expectedItem.setId(1);
        expectedItem.setUserId(1);
        expectedItem.setComments(List.of(createCommentDto()));

        when(itemMapper.toDto(item)).thenReturn(expectedItem);
        when(commentRepository.findByItemId(item.getId())).thenReturn(List.of(createComment()));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(createCommentDto());
        when(userRepository.findById(1)).thenReturn(Optional.of(createUser()));

        List<ItemDto> result = itemService.searchItem(searchingSubstring, userId);

        assertThat(result, equalTo(List.of(expectedItem)));
    }

    @Test
    void getAllItemsByUser_WhenUserHasItems_ShouldReturnItemsWithDetails() {
        Integer userId = 1;
        Item item = buildItem();
        List<Item> items = List.of(item);
        when(itemRepository.findAllByUserId(userId)).thenReturn(items);

        when(bookingRepository.findLastBookingForItem(eq(item.getId()), eq(userId), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findNextBookingForItem(eq(item.getId()), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        CommentDto commentDto = createCommentDto();
        when(commentRepository.findByItemId(item.getId())).thenReturn(List.of(createComment()));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDto);
        when(userRepository.findById(1)).thenReturn(Optional.of(createUser()));

        ItemDto expectedItem = createItemDto();
        expectedItem.setId(1);
        expectedItem.setUserId(1);
        expectedItem.setComments(List.of(commentDto));

        when(itemMapper.toDto(eq(item), isNull(), isNull(), anyList())).thenReturn(expectedItem);

        List<ItemDto> result = itemService.getAllItemsByUser(userId);

        assertThat(result, equalTo(List.of(expectedItem)));
    }

    @Test
    void createItem_WhenNameIsBlank_ShouldThrowValidationException() {
        Integer userId = 1;
        ItemDto request = createItemDto();
        request.setName("");
        when(userRepository.findById(userId)).thenReturn(Optional.of(createUser()));
        assertThrows(ValidationException.class, () -> itemService.create(request, userId));
    }

    @Test
    void createItem_WhenDescriptionIsBlank_ShouldThrowValidationException() {
        Integer userId = 1;
        ItemDto request = createItemDto();
        request.setDescription("   ");
        when(userRepository.findById(userId)).thenReturn(Optional.of(createUser()));
        assertThrows(ValidationException.class, () -> itemService.create(request, userId));
    }

    @Test
    void createItem_WhenAvailableIsNull_ShouldThrowValidationException() {
        Integer userId = 1;
        ItemDto request = createItemDto();
        request.setAvailable(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(createUser()));
        assertThrows(ValidationException.class, () -> itemService.create(request, userId));
    }

    @Test
    void getItemById_WhenIdIsZero_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> itemService.getItemById(0));
    }

    @Test
    void getItemById_WhenIdIsNull_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> itemService.getItemById(null));
    }

    @Test
    void createItem_WhenUserIdIsNull_ShouldThrowValidationException() {
        ItemDto request = createItemDto();
        assertThrows(ValidationException.class, () -> itemService.create(request, null));
    }

    @Test
    void createItem_WhenUserIdIsZero_ShouldThrowValidationException() {
        ItemDto request = createItemDto();
        assertThrows(ValidationException.class, () -> itemService.create(request, 0));
    }

    @Test
    void updateItem_WhenItemIdIsNull_ShouldThrowValidationException() {
        ItemDto request = createItemDto();
        assertThrows(ValidationException.class, () -> itemService.update(null, request, 1));
    }

    @Test
    void getItemByIdWithDetails_WhenUserIdIsNull_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> itemService.getItemByIdWithDetails(1, null));
    }

    @Test
    void getAllItemsByUser_WhenUserIdIsNull_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> itemService.getAllItemsByUser(null));
    }

    @Test
    void addComment_WhenCommentTextIsNull_ShouldThrowValidationException() {
        CommentRequestDto request = new CommentRequestDto();
        request.setText(null);
        assertThrows(ValidationException.class, () -> itemService.addComment(1, 1, request));
    }

    @Test
    void addComment_WhenCommentTextIsBlank_ShouldThrowValidationException() {
        CommentRequestDto request = new CommentRequestDto();
        request.setText("   ");
        assertThrows(ValidationException.class, () -> itemService.addComment(1, 1, request));
    }

    @Test
    void searchItem_WhenTextIsBlank_ShouldReturnEmptyList() {
        Integer userId = 1;
        List<ItemDto> result = itemService.searchItem("   ", userId);
        Assertions.assertTrue((result).isEmpty());
    }

    @Test
    void searchItem_WhenTextIsNull_ShouldReturnEmptyList() {
        Integer userId = 1;
        List<ItemDto> result = itemService.searchItem(null, userId);
        Assertions.assertTrue((result).isEmpty());
    }

}