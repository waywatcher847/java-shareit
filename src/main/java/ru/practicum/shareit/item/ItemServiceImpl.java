package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.comment.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.*;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Service
@Slf4j
@Qualifier("ItemServiceImpl")
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                         UserRepository userRepository,
                         BookingRepository bookingRepository,
                         CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }
    @Override
    @Transactional
    public CommentDto addComment(CommentDto commentDto, Integer userId, Integer itemId) {
        log.info("ItemServiceImpl->addComment start");
        Optional<Booking> booking = bookingRepository.findByBookerIdAndItemIdAndStatusAndEndBefore(userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());

        if (booking.isEmpty()) {
            String error = "Wrong time or wrong guy";
            log.warn(error);
            throw new ValidationException(error);
        }

        Comment comment = CommentMapper.toComment(commentDto, booking.get().getBooker(), booking.get().getItem());
        Comment commentResult = commentRepository.save(comment);
        log.info("ItemServiceImpl->addComment end");
        return CommentMapper.toCommentDto(commentResult);
    }

    @Override
    @Transactional
    public void deleteItem(Integer itemId, Integer userId) {
        log.info("ItemServiceImpl->deleteItem start");
        validateUser(userId);
        Item item = validateItem(itemId, userId);
        itemRepository.delete(item);
        log.info("ItemServiceImpl->deleteItem end");
    }

    @Override
    public List<ItemDtoOwner> getUserItems(Integer userId) {
        log.info("ItemServiceImpl->getUserItems start");
        validateUser(userId);

        List<ItemDtoOwner> itemOwnerDtoList = itemRepository.findByOwner_Id(userId).stream()
                .filter(Objects::nonNull)
                .map(ItemMapper::mapToItemOwnerDto)
                .toList();

        if (itemOwnerDtoList.isEmpty()) {
            log.info("ItemServiceImpl->getUserItems end");
            return List.of();
        }


        List<Integer> itemIds = itemOwnerDtoList.stream()
                .filter(Objects::nonNull)
                .map(ItemDtoOwner::getId)
                .collect(Collectors.toList());

        List<ItemDtoOwner> itemOwnerDto = setBookingDates(itemOwnerDtoList,
                bookingRepository.getBookingsByLast(itemIds, LocalDateTime.now(), BookingStatus.APPROVED),
                bookingRepository.getBookingsByNext(itemIds, LocalDateTime.now(), BookingStatus.APPROVED));

        List<Comment> comments = commentRepository.getCommentByAuthorIdAndItemId(itemIds);
        List<ItemDtoOwner> itemOwnerDtoResult = setComments(itemOwnerDto, comments);

        log.info("ItemServiceImpl->getUserItems end");
        return itemOwnerDtoResult;
    }
    @Override
    @Transactional
    public ItemDto createItem(ItemDtoNew newItemRequestDto, Integer userId) {
        log.info("ItemServiceImpl->createItem start");
        User user = validateUser(userId);
        Item item = ItemMapper.mapToItem(newItemRequestDto);
        item.setOwner(user);

        Item itemResult = itemRepository.save(item);
        log.info("ItemServiceImpl->createItem end");
        return toItemDtoWithUser(itemResult, user);
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDtoUpdate updateItemRequestDto, Integer userId, Integer itemId) {
        log.info("ItemServiceImpl->updateItem start");
        User user = validateUser(userId);
        Item item = validateItem(itemId, userId);
        Item itemUpdate = ItemMapper.updateItemField(item, updateItemRequestDto);
        Item itemResult = itemRepository.save(itemUpdate);
        log.info("ItemServiceImpl->updateItem end");
        return toItemDtoWithUser(itemResult, user);
    }

    @Override
    public ItemDto getByIdItem(Integer itemId, Integer userId) {
        log.info("ItemServiceImpl->getByIdItem start");
        Item item = itemRepository.findByWithOwner(itemId)
                .orElseThrow(() -> new NotFoundException("Item with ID " + itemId + " not found"));
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
                itemDto.setLastBooking(bookingLasts.getFirst().getLastBooking());
            }

            if (!bookingNexts.isEmpty()) {
                itemDto.setNextBooking(bookingNexts.getFirst().getNextBooking());
            }
        }

        log.info("ItemServiceImpl->getByIdItem end");
        return itemDto;
    }
    @Override
    public List<ItemDto> searchItem(String text) {
        log.info("ItemServiceImpl->searchItem start");
        if (text == null || text.isBlank()) {
            log.info("ItemServiceImpl->searchItem end");
            return new ArrayList<>();
        }

        List<ItemDto> itemList = itemRepository.findBySearchItem(text.trim().toLowerCase(Locale.ROOT))
                .stream()
                .filter(Objects::nonNull)
                .map(item -> toItemDtoWithUser(item, item.getOwner()))
                .toList();

        log.info("ItemServiceImpl->searchItem end");
        return itemList;
    }

    private User validateUser(Integer userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            String error = String.format("User with ID: %d not found", userId);
            log.warn(error);
            throw new NotFoundException(error);
        }
        return optionalUser.get();
    }

    private Item validateItem(Integer itemId, Integer ownerId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            String error = String.format("Item with ID: %d not found", itemId);
            log.warn(error);
            throw new NotFoundException(error);
        }

        if (!optionalItem.get().getOwner().getId().equals(ownerId)) {
            String error = "User is not the owner!";
            log.error(error);
            throw new ValidationException(error);
        }
        return optionalItem.get();
    }

    private ItemDto toItemDtoWithUser(Item item, User user) {
        ItemDto itemDto = ItemMapper.mapToItemDto(item);
        UserDto userDto = UserMapper.mapToUserDto(user);
        itemDto.setOwner(userDto);
        return itemDto;
    }

    private List<ItemDtoOwner> setBookingDates(
            List<ItemDtoOwner> ownerDto,
            List<BookingLast> bookingLasts,
            List<BookingNext> bookingNexts) {
        Map<Integer, LocalDateTime> bookingLastsMap = bookingLasts.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BookingLast::getId, BookingLast::getLastBooking));

        Map<Integer, LocalDateTime> bookingNextMap = bookingNexts.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BookingNext::getId, BookingNext::getNextBooking));

        for (ItemDtoOwner ownerDtoItem : ownerDto) {
            if (bookingLastsMap.containsKey(ownerDtoItem.getId())) {
                ownerDtoItem.setLastBooking(bookingLastsMap.get(ownerDtoItem.getId()));
            }
            if (bookingNextMap.containsKey(ownerDtoItem.getId())) {
                ownerDtoItem.setNextBooking(bookingNextMap.get(ownerDtoItem.getId()));
            }
        }
        return ownerDto;
    }

    private List<ItemDtoOwner> setComments(List<ItemDtoOwner> itemOwnerDtos, List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return itemOwnerDtos;
        }

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
        return itemOwnerDtos;
    }
}