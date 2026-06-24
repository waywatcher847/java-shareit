package ru.practicum.shareit.validation;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

@Slf4j
public class ValidationTool {
    
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    private ValidationTool() {
        throw new UnsupportedOperationException();
    }

    public static void checkId(Integer id, String level, String description) {
        if (id == null || id < 1) {
            throw new ValidationException(level + ": " + description);
        }
    }

    public Booking getBookingByIdOrThrow(Integer bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.info("Бронирование с id {} отсутствует", bookingId);
                    return new NotFoundException(String.format("Бронирование с id %d отсутствует", bookingId));
                });
    }

    public User getUserByIdOrThrow(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("Пользователь с id {} отсутствует", userId);
                    return new NotFoundException(String.format("Пользователь с id %d отсутствует", userId));
                });
    }

    public Item getItemByIdOrThrow(Integer itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.info("Вещь с id {} отсутствует", itemId);
                    return new NotFoundException(String.format("Вещь с id %d отсутствует", itemId));
                });
    }

    public ItemRequest getItemRequestByIdOrThrow(Integer requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.info("Запрос на вещь с id {} отсутствует", requestId);
                    return new NotFoundException(String.format("Запрос на вещь с id %d отсутствует", requestId));
                });
    }

    public void checkUserHasCompletedBooking(Integer authorId, Integer itemId) {
        LocalDateTime now = LocalDateTime.now();
        boolean isExistsCompletedBookings = bookingRepository.existsByBookerIdAndItemIdAndEndIsBefore(authorId, itemId, now);

        if (!isExistsCompletedBookings) {
            throw new ValidationException(String.format("User id %d doesn't have item id %d", authorId, itemId));
        }
    }

}
