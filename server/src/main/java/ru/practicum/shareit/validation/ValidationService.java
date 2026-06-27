package ru.practicum.shareit.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingRepository bookingRepository;

    public User validateUserExists(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    String error = String.format("User with ID: %d not found", userId);
                    log.warn(error);
                    return new NotFoundException(error);
                });
    }

    public Item validateItemExists(Integer itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    String error = String.format("Item with ID: %d not found", itemId);
                    log.warn(error);
                    return new NotFoundException(error);
                });
    }

    public Item validateItemExistsWithOwner(Integer itemId) {
        return itemRepository.findByWithOwner(itemId)
                .orElseThrow(() -> {
                    String error = String.format("Item with ID: %d not found", itemId);
                    log.warn(error);
                    return new NotFoundException(error);
                });
    }

    public Item validateItemOwner(Integer itemId, Integer ownerId) {
        Item item = validateItemExists(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            String error = "User is not the owner!";
            log.error(error);
            throw new AccessDeniedException(error);
        }
        return item;
    }

    public ItemRequest validateItemRequestExists(Integer requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    String error = String.format("Request with ID: %d not found", requestId);
                    log.warn(error);
                    return new NotFoundException(error);
                });
    }

    public Booking validateBookingExists(Integer bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    String error = String.format("Booking with ID: %d does not exist", bookingId);
                    log.warn(error);
                    return new NotFoundException(error);
                });
    }

    public void validateEmailNotTaken(String email) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            String error = String.format("Email: %s is already taken by another user", email);
            log.warn(error);
            throw new ConflictException(error);
        }
    }

    public Booking validateBookingForComment(Integer userId, Integer itemId) {
        return bookingRepository.findByBookerIdAndItemIdAndStatusAndEndBefore(
                        userId, itemId, BookingStatus.APPROVED, LocalDateTime.now())
                .orElseThrow(() -> {
                    String error = "booking not found";
                    log.warn(error);
                    return new ValidationException(error);
                });
    }
}