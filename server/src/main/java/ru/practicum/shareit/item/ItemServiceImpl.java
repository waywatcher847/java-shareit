package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.common.item.ItemDto;
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
import ru.practicum.shareit.validation.ValidationTool;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final BookingMapper bookingMapper;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private static final String THIS_CLASS = "ItemService";

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Integer userId) {
        log.info("ItemServiceImpl->create start");
        log.info("itemDto={}, userId={}", itemDto, userId);

        ValidationTool.checkId(userId, THIS_CLASS, "user_id must not be null when creating an item");

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("owner not found for id = " + userId));

        validateItemFields(itemDto);

        Item item = itemMapper.toEntity(itemDto);
        item.setUserId(userId);

        Item savedItem = repository.save(item);
        log.info("Created item with ID: {} for user with ID: {}", savedItem.getId(), userId);

        ItemDto savedItemDto = itemMapper.toDto(savedItem);
        savedItemDto.setComments(Collections.emptyList());

        log.info("ItemServiceImpl->create end");
        return savedItemDto;
    }

    @Override
    @Transactional
    public ItemDto update(Integer id, ItemDto itemDto, Integer userId) {
        log.info("ItemServiceImpl->update start");
        log.info("id={}, itemDto={}, userId={}", id, itemDto, userId);

        ValidationTool.checkId(id, THIS_CLASS, "Item cannot be updated with id = null");

        Item existingItem = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Item with id = " + id + " not found")
        );

        if (!existingItem.getUserId().equals(userId)) {
            throw new NotFoundException("Owner id =/= provided id");
        }

        itemMapper.updateItemFromDto(itemDto, existingItem);
        existingItem.setId(id);

        Item updatedItem = repository.save(existingItem);

        ItemDto updatedItemDto = itemMapper.toDto(updatedItem);
        updatedItemDto.setComments(getCommentsForItem(id));

        log.info("ItemServiceImpl->update end");
        return updatedItemDto;
    }

    @Override
    public ItemDto getItemById(Integer id) {
        log.info("ItemServiceImpl->getItemById start");
        log.info("id={}", id);

        ValidationTool.checkId(id, THIS_CLASS, "Item cannot be found with id = null");

        Item item = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Item with id = " + id + " not found")
        );

        ItemDto itemDto = itemMapper.toDto(item);
        itemDto.setComments(getCommentsForItem(id));

        log.info("ItemServiceImpl->getItemById end");
        return itemDto;
    }

    @Override
    public ItemDto getItemByIdWithDetails(Integer id, Integer userId) {
        log.info("ItemServiceImpl->getItemByIdWithDetails start");
        log.info("id={}, userId={}", id, userId);

        ValidationTool.checkId(id, THIS_CLASS, "Item cannot be found with id = null");
        ValidationTool.checkId(userId, THIS_CLASS, "User id cannot be null");

        Item item = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Item with id = " + id + " not found")
        );

        LocalDateTime now = LocalDateTime.now();
        BookingDto lastBooking = null;
        BookingDto nextBooking = null;

        if (item.getUserId().equals(userId)) {
            lastBooking = bookingRepository
                    .findLastBookingForItem(id, userId, now, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .map(bookingMapper::toDto)
                    .orElse(null);

            nextBooking = bookingRepository
                    .findNextBookingForItem(id, now, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .map(bookingMapper::toDto)
                    .orElse(null);
        }

        List<CommentDto> comments = getCommentsForItem(id);
        ItemDto result = itemMapper.toDto(item, lastBooking, nextBooking, comments);

        log.info("ItemServiceImpl->getItemByIdWithDetails end");
        return result;
    }

    @Override
    public List<ItemDto> getAllItemsByUser(Integer userId) {
        log.info("ItemServiceImpl->getAllItemsByUser start");
        log.info("userId={}", userId);

        ValidationTool.checkId(userId, THIS_CLASS, "Items cannot be found with user_id = null");

        List<Item> items = repository.findAllByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        List<ItemDto> result = items.stream()
                .map(item -> {
                    BookingDto lastBooking = bookingRepository
                            .findLastBookingForItem(item.getId(), userId, now, PageRequest.of(0, 1))
                            .stream()
                            .findFirst()
                            .map(bookingMapper::toDto)
                            .orElse(null);

                    BookingDto nextBooking = bookingRepository
                            .findNextBookingForItem(item.getId(), now, PageRequest.of(0, 1))
                            .stream()
                            .findFirst()
                            .map(bookingMapper::toDto)
                            .orElse(null);

                    List<CommentDto> comments = getCommentsForItem(item.getId());

                    return itemMapper.toDto(item, lastBooking, nextBooking, comments);
                })
                .collect(Collectors.toList());

        log.info("ItemServiceImpl->getAllItemsByUser end");
        return result;
    }

    @Override
    public List<ItemDto> searchItem(String text, Integer userId) {
        log.info("ItemServiceImpl->searchItem start");
        log.info("text={}, userId={}", text, userId);

        if (text == null || text.trim().isEmpty()) {
            log.info("ItemServiceImpl->searchItem end");
            return Collections.emptyList();
        }
        log.info("Searching items by text: '{}' for user {}", text, userId);

        List<Item> items = repository.searchItem(text);

        List<ItemDto> result = items.stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);
                    dto.setComments(getCommentsForItem(item.getId()));
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("ItemServiceImpl->searchItem end");
        return result;
    }

    @Override
    @Transactional
    public CommentDto addComment(Integer userId, Integer itemId, CommentRequestDto commentRequestDto) {
        boolean hasPastBooking = false;

        log.info("ItemServiceImpl->addComment start");
        log.info("userId={}, itemId={}, commentRequestDto={}", userId, itemId, commentRequestDto);

        if (commentRequestDto.getText() == null || commentRequestDto.getText().isBlank()) {
            log.error("Comment text is empty");
            throw new ValidationException("Comment text cannot be empty");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));
        log.info("Author found: {} (ID={})", author.getName(), author.getId());

        Item item = repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with ID " + itemId + " not found"));
        log.info("Item found: {} (ID={})", item.getName(), item.getId());

        LocalDateTime now = LocalDateTime.now(); //Сервер живет в прошлом
        log.info("Current time: {}", now);

        List<Booking> approvedBookings = bookingRepository.findByUserIdAndItemIdAndStatus(
                userId, itemId, BookingStatus.APPROVED);

        log.info("APPROVED bookings for user {} for item {}: {}", userId, itemId, approvedBookings.size());

        if (approvedBookings.isEmpty()) {
            log.error("User {} has no APPROVED bookings for item {}", userId, itemId);
            throw new ValidationException("Can only comment after an approved booking");
        }

        for (Booking booking : approvedBookings) {
            boolean isPast = booking.getEndDate().isBefore(now);
            log.info("Booking: ID={}, status={}, start={}, end={}, PAST? {}",
                    booking.getId(), booking.getStatus(), booking.getStartDate(), booking.getEndDate(),
                    isPast);

            if (isPast) {
                hasPastBooking = true;
                log.info("Found PAST (completed) booking with ID={}", booking.getId());
            } else {
                log.info("Found ACTIVE booking with ID={}", booking.getId());
            }
        }

        if (hasPastBooking) {
            log.info("Past booking found - creating comment");

            Comment comment = Comment.builder()
                    .text(commentRequestDto.getText())
                    .itemId(itemId)
                    .userId(userId)
                    .created(Instant.now())
                    .build();

            Comment savedComment = commentRepository.save(comment);
            log.info("Comment saved with ID: {}", savedComment.getId());

            CommentDto savedCommentDto = commentMapper.toDto(savedComment);
            savedCommentDto.setAuthorName(author.getName());

            log.info("Added comment: {}", savedCommentDto);

            log.info("ItemServiceImpl->addComment end");
            return savedCommentDto;
        }

        log.error("User {} has only active APPROVED bookings for item {}", userId, itemId);
        throw new ValidationException("Cannot create comment before the booking is finished");

    }

    private List<CommentDto> getCommentsForItem(Integer itemId) {
        List<Comment> comments = commentRepository.findByItemId(itemId);

        return comments.stream()
                .map(comment -> {
                    CommentDto dto = commentMapper.toDto(comment);
                    userRepository.findById(comment.getUserId())
                            .ifPresent(author -> dto.setAuthorName(author.getName()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void validateItemFields(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Item status cannot be empty");
        }
    }
}