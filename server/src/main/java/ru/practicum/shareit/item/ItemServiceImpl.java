package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentDtoRequest;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.item.ItemDtoRequest;
import ru.practicum.common.item.ItemDtoOwner;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingLast;
import ru.practicum.shareit.booking.model.BookingNext;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.validation.ValidationService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Qualifier("ItemServiceImpl")
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ValidationService validationService;

    @Override
    @Transactional
    public CommentDto addComment(CommentDtoRequest commentDto, Integer userId, Integer itemId) {
        log.info("ItemServiceImpl->addComment start, commentDto={}, userId={}, itemId={}", commentDto, userId, itemId);

        Booking booking = validationService.validateBookingForComment(
                userId,
                itemId);

        Comment comment = CommentMapper.toComment(commentDto, booking.getBooker(), booking.getItem());
        Comment commentResult = commentRepository.save(comment);
        CommentDto result = CommentMapper.toCommentDto(commentResult);

        log.info("ItemServiceImpl->addComment end, result={}", result);
        return result;
    }

    @Override
    @Transactional
    public void deleteItem(Integer itemId, Integer userId) {
        log.info("ItemServiceImpl->deleteItem start, itemId={}, userId={}", itemId, userId);

        validationService.validateUserExists(userId);
        Item item = validationService.validateItemOwner(itemId, userId);

        itemRepository.delete(item);

        log.info("ItemServiceImpl->deleteItem end");
    }

    @Override
    public List<ItemDtoOwner> getUserItems(Integer userId) {
        log.info("ItemServiceImpl->getUserItems start, userId={}", userId);

        validationService.validateUserExists(userId);

        List<ItemDtoOwner> itemOwnerDtoList = itemRepository.findByOwner_Id(userId).stream()
                .filter(Objects::nonNull)
                .map(ItemMapper::mapToItemOwnerDto)
                .toList();

        List<ItemDtoOwner> result;
        if (itemOwnerDtoList.isEmpty()) {
            result = List.of();
        } else {
            List<Integer> itemIds = itemOwnerDtoList.stream()
                    .filter(Objects::nonNull)
                    .map(ItemDtoOwner::getId)
                    .collect(Collectors.toList());

            List<ItemDtoOwner> itemOwnerDto = setBookingDates(itemOwnerDtoList,
                    bookingRepository.getBookingsByLast(itemIds, LocalDateTime.now(), BookingStatus.APPROVED),
                    bookingRepository.getBookingsByNext(itemIds, LocalDateTime.now(), BookingStatus.APPROVED));

            List<Comment> comments = commentRepository.getCommentByAuthorIdAndItemId(itemIds);
            result = setComments(itemOwnerDto, comments);
        }

        log.info("ItemServiceImpl->getUserItems end, result size={}", result.size());
        return result;
    }

    @Override
    @Transactional
    public ItemDto create(ItemDtoRequest newItemRequestDto, Integer userId) {
        log.info("ItemServiceImpl->create start, newItemRequestDto={}, userId={}", newItemRequestDto, userId);

        User user = validationService.validateUserExists(userId);
        Item item = ItemMapper.mapToItem(newItemRequestDto);
        item.setOwner(user);

        if (newItemRequestDto.getRequestId() != null) {
            ItemRequest request = validationService.validateItemRequestExists(newItemRequestDto.getRequestId());
            item.setRequest(request);
        }

        Item itemResult = itemRepository.save(item);
        log.info("DEBUG: Saved item with id={}, request_id={}",
                itemResult.getId(),
                itemResult.getRequest() != null ? itemResult.getRequest().getId() : "NULL");

        ItemDto result = toItemDtoWithUser(itemResult, user);
        log.info("ItemServiceImpl->create end, result={}", result);
        return result;
    }

    @Override
    @Transactional
    public ItemDto update(ItemDtoRequest updateItemRequestDto, Integer itemId, Integer userId) {
        log.info("ItemServiceImpl->update start, updateItemRequestDto={}, itemId={}, userId={}",
                updateItemRequestDto, itemId, userId);

        User user = validationService.validateUserExists(userId);
        Item item = validationService.validateItemOwner(itemId, userId);
        Item itemUpdate = ItemMapper.updateItemField(item, updateItemRequestDto);
        Item itemResult = itemRepository.save(itemUpdate);

        log.info("DEBUG: Updated item with id={}, request_id={}",
                itemResult.getId(),
                itemResult.getRequest() != null ? itemResult.getRequest().getId() : "NULL");

        ItemDto result = toItemDtoWithUser(itemResult, user);
        log.info("ItemServiceImpl->update end, result={}", result);
        return result;
    }

    @Override
    public ItemDto getById(Integer itemId, Integer userId) {
        log.info("ItemServiceImpl->getById start, itemId={}, userId={}", itemId, userId);

        Item item = validationService.validateItemExistsWithOwner(itemId);
        List<Comment> comments = commentRepository.getCommentByItemId(itemId);

        ItemDto itemDto = ItemMapper.mapToItemDto(item);
        itemDto.setOwner(UserMapper.mapToUserDto(item.getOwner()));
        itemDto.setComments(comments.stream()
                .filter(Objects::nonNull)
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));

        if (item.getOwner().getId().equals(userId)) {
            List<Integer> itemIds = List.of(item.getId());
            List<BookingLast> bookingLasts = bookingRepository
                    .getBookingsByLast(itemIds, LocalDateTime.now(), BookingStatus.APPROVED);
            List<BookingNext> bookingNexts = bookingRepository
                    .getBookingsByNext(itemIds, LocalDateTime.now(), BookingStatus.APPROVED);

            if (!bookingLasts.isEmpty()) {
                itemDto.setLastBooking(BookingMapper.mapToBookingDto(bookingLasts.getFirst().getLastBooking()));
            }

            if (!bookingNexts.isEmpty()) {
                itemDto.setNextBooking(BookingMapper.mapToBookingDto(bookingNexts.getFirst().getNextBooking()));
            }
        }

        log.info("ItemServiceImpl->getById end, result={}", itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDto> getByText(String text, Integer userId) {
        log.info("ItemServiceImpl->getByText start, text={}, userId={}", text, userId);

        List<ItemDto> result;
        if (text == null || text.isBlank()) {
            result = new ArrayList<>();
        } else {
            result = itemRepository.findByText(text.trim().toLowerCase(Locale.ROOT))
                    .stream()
                    .filter(Objects::nonNull)
                    .map(item -> toItemDtoWithUser(item, item.getOwner()))
                    .toList();
        }

        log.info("ItemServiceImpl->getByText end, result size={}", result.size());
        return result;
    }

    private ItemDto toItemDtoWithUser(Item item, User user) {
        log.info("ItemServiceImpl->toItemDtoWithUser start, item={}, user={}", item, user);
        ItemDto itemDto = ItemMapper.mapToItemDto(item);
        UserDto userDto = UserMapper.mapToUserDto(user);
        itemDto.setOwner(userDto);
        log.info("ItemServiceImpl->toItemDtoWithUser end, result={}", itemDto);
        return itemDto;
    }

    private List<ItemDtoOwner> setBookingDates(
            List<ItemDtoOwner> ownerDto,
            List<BookingLast> bookingLasts,
            List<BookingNext> bookingNexts) {
        log.info("ItemServiceImpl->setBookingDates start, ownerDto size={}, bookingLasts size={}, bookingNexts size={}",
                ownerDto.size(), bookingLasts.size(), bookingNexts.size());
        Map<Integer, Booking> bookingLastsMap = bookingLasts.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BookingLast::getId, BookingLast::getLastBooking));

        Map<Integer, Booking> bookingNextMap = bookingNexts.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BookingNext::getId, BookingNext::getNextBooking));

        for (ItemDtoOwner ownerDtoItem : ownerDto) {
            if (bookingLastsMap.containsKey(ownerDtoItem.getId())) {
                ownerDtoItem.setLastBooking(BookingMapper.mapToBookingDto(bookingLastsMap.get(ownerDtoItem.getId())));
            }
            if (bookingNextMap.containsKey(ownerDtoItem.getId())) {
                ownerDtoItem.setNextBooking(BookingMapper.mapToBookingDto(bookingNextMap.get(ownerDtoItem.getId())));
            }
        }
        log.info("ItemServiceImpl->setBookingDates end, result size={}", ownerDto.size());
        return ownerDto;
    }

    private List<ItemDtoOwner> setComments(List<ItemDtoOwner> itemOwnerDtos, List<Comment> comments) {
        log.info("ItemServiceImpl->setComments start, itemOwnerDtos size={}, comments size={}",
                itemOwnerDtos.size(), comments == null ? 0 : comments.size());

        List<ItemDtoOwner> result;
        if (comments == null || comments.isEmpty()) {
            return itemOwnerDtos;
        } else {

            Map<Integer, List<Comment>> commentsMap = comments.stream()
                    .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

            for (ItemDtoOwner ownerDtoItem : itemOwnerDtos) {
                List<Comment> itemComments = commentsMap.get(ownerDtoItem.getId());
                if (itemComments != null) {
                    ownerDtoItem.setComments(itemComments.stream()
                            .filter(Objects::nonNull)
                            .map(CommentMapper::toCommentDto)
                            .collect(Collectors.toList()));
                } else {
                    ownerDtoItem.setComments(List.of());
                }
            }
            result = itemOwnerDtos;
        }

        log.info("ItemServiceImpl->setComments end, result size={}", result.size());
        return result;
    }
}