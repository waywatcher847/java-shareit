package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTests {

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
    private CommentMapper commentMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private ItemMapper itemMapper;

    private Item createItem() {
        return Item.builder()
                .id(1)
                .name("Item")
                .description("Desc")
                .available(true)
                .userId(2)
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(1)
                .name("Author")
                .email("author@mail.ru")
                .build();
    }

    private Comment createComment(Instant created) {
        return Comment.builder()
                .id(1)
                .text("Great item!")
                .itemId(1)
                .userId(1)
                .created(created)
                .build();
    }

    private CommentRequestDto createCommentRequest(String text) {
        CommentRequestDto request = new CommentRequestDto();
        request.setText(text);
        return request;
    }

    private Booking createBooking(LocalDateTime endDate) {
        Booking booking = new Booking();
        booking.setId(1);
        booking.setUserId(1);
        booking.setItemId(1);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStartDate(endDate.minusHours(2));
        booking.setEndDate(endDate);
        return booking;
    }

    private CommentDto createCommentDto(Instant created) {
        CommentDto dto = new CommentDto();
        dto.setId(1);
        dto.setText("Great item!");
        dto.setItemId(1);
        dto.setUserId(1);
        dto.setAuthorName("Author");
        dto.setCreated(created);
        return dto;
    }

    @Test
    void ItemService_WhenAddingCommentWithNonExistentUser_ThrowsNotFoundException() {
        Integer authorId = 999;
        Integer itemId = 1;
        CommentRequestDto request = createCommentRequest("Great item!");

        when(userRepository.findById(authorId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(authorId, itemId, request));

        assertEquals("User with ID " + authorId + " not found", exception.getMessage());
    }

    @Test
    void ItemService_WhenAddingCommentWithValidData_ReturnsCommentDto() {
        Integer authorId = 1;
        Integer itemId = 1;
        CommentRequestDto request = createCommentRequest("Great item!");
        Instant created = Instant.now();

        User author = createUser();
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        Item item = createItem();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Booking pastBooking = createBooking(LocalDateTime.now().minusDays(1));
        when(bookingRepository.findByUserIdAndItemIdAndStatus(authorId, itemId, BookingStatus.APPROVED))
                .thenReturn(List.of(pastBooking));

        Comment savedComment = createComment(created);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto expectedComment = createCommentDto(created);
        when(commentMapper.toDto(savedComment)).thenReturn(expectedComment);

        CommentDto result = itemService.addComment(authorId, itemId, request);

        verify(commentRepository).save(any(Comment.class));
        assertThat(result, equalTo(expectedComment));
    }

    @Test
    void ItemService_WhenAddingCommentWithNoApprovedBookings_ThrowsValidationException() {
        Integer authorId = 1;
        Integer itemId = 1;
        CommentRequestDto request = createCommentRequest("Great item!");

        User author = createUser();
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        Item item = createItem();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        when(bookingRepository.findByUserIdAndItemIdAndStatus(authorId, itemId, BookingStatus.APPROVED))
                .thenReturn(Collections.emptyList());

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(authorId, itemId, request));

        assertEquals("Can only comment after an approved booking", exception.getMessage());
    }

    @Test
    void ItemService_WhenAddingCommentWithNonExistentItem_ThrowsNotFoundException() {
        Integer authorId = 1;
        Integer itemId = 999;
        CommentRequestDto request = createCommentRequest("Great item!");

        User author = createUser();
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(authorId, itemId, request));

        assertEquals("Item with ID " + itemId + " not found", exception.getMessage());
    }

    @Test
    void ItemService_WhenAddingCommentWithEmptyText_ThrowsValidationException() {
        Integer authorId = 1;
        Integer itemId = 1;
        CommentRequestDto request = createCommentRequest("");

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(authorId, itemId, request));

        assertEquals("Comment text cannot be empty", exception.getMessage());
    }

    @Test
    void ItemService_WhenAddingCommentWithActiveBooking_ThrowsValidationException() {
        Integer authorId = 1;
        Integer itemId = 1;
        CommentRequestDto request = createCommentRequest("Great item!");

        User author = createUser();
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        Item item = createItem();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Booking futureBooking = createBooking(LocalDateTime.now().plusDays(1));
        when(bookingRepository.findByUserIdAndItemIdAndStatus(authorId, itemId, BookingStatus.APPROVED))
                .thenReturn(List.of(futureBooking));

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(authorId, itemId, request));

        assertEquals("Cannot create comment before the booking is finished", exception.getMessage());
    }
}