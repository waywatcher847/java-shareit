package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingDtoRequest;
import ru.practicum.common.booking.BookingState;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BookingDto create(BookingDtoRequest bookingDtoNew, Integer bookerId) {
        log.info("BookingServiceImpl->create start");
        log.info(">>> " + bookingDtoNew);

        User booker = validateUser(bookerId);

        Item item = validateItem(bookingDtoNew.getItemId());
        log.info(">>> " + item);

        if (booker.equals(item.getOwner())) {
            String error = "Item owner cannot book the item ";
            log.error(error);
            throw new ValidationException(error);
        }

        if (!item.getAvailable()) {
            String error = "Item is unavailable";
            log.error(error);
            throw new ValidationException(error);
        }

        if (isBookingOverlapping(bookingDtoNew)) {
            String error = "item is already booked";
            log.error(error);
            throw new ValidationException(error);
        }
        Booking booking = Booking.builder().start(bookingDtoNew.getStart()).end(bookingDtoNew.getEnd()).item(item).booker(booker).status(BookingStatus.WAITING).build();

        log.info(">>> !!!" + booking);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("BookingServiceImpl->create end");
        return BookingMapper.mapToBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approve(Integer bookingId, Integer userId, Boolean approved) {
        log.info("BookingServiceImpl->approve start");
        if (approved == null) {
            String error = "must be true or false";
            log.warn(error);
            throw new NotFoundException(error);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ValidationException("User with ID: " + userId + " not found"));

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException(String.format("Booking with ID: %d does not exist", bookingId)));

        if (booking.getItem().getOwner().equals(user)) {
            if (booking.getStatus().equals(BookingStatus.WAITING)) {
                boolean hasOverlap = bookingRepository.existsByItemIdAndStatusAndEndGreaterThanEqualAndStartLessThanEqual(booking.getItem().getId(), BookingStatus.APPROVED, booking.getStart(), booking.getEnd());

                if (!hasOverlap) {
                    booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
                    bookingRepository.save(booking);
                } else {
                    if (approved) {
                        String error = "Error: the item is already booked";
                        log.error(error);
                        throw new ValidationException(error);
                    } else {
                        booking.setStatus(BookingStatus.REJECTED);
                        bookingRepository.save(booking);
                    }
                }
            } else {
                String error = "Error: already changed";
                log.error(error);
                throw new ValidationException(error);
            }
        } else {
            String error = String.format("user with ID: %d is not the item owner", userId);
            log.warn(error);
            throw new AccessDeniedException(error);
        }

        log.info("BookingServiceImpl->approve end");
        return BookingMapper.mapToBookingDto(booking);
    }

    @Override
    public BookingDto getById(Integer bookingId, Integer userId) {
        log.info("BookingServiceImpl->getById start");
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException(String.format("Booking with ID: %d does not exist", bookingId)));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            String error = "Not the item owner or the booking author";
            log.warn(error);
            throw new AccessDeniedException(error);
        }

        log.info("BookingServiceImpl->getById end");
        return BookingMapper.mapToBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Integer bookerId, BookingState bookingState) {
        log.info("BookingServiceImpl->getUserBookings start");
        validateUser(bookerId);

        List<Booking> bookings = getBookingsByState(bookerId, bookingState, true);

        log.info("BookingServiceImpl->getUserBookings end");
        return bookings.stream().filter(Objects::nonNull).map(BookingMapper::mapToBookingDto).toList();
    }

    @Override
    public List<BookingDto> getOwnerBookings(Integer ownerId, BookingState bookingState) {
        log.info("BookingServiceImpl->getOwnerBookings start");
        validateUser(ownerId);

        List<Booking> bookings = getBookingsByState(ownerId, bookingState, false);

        log.info("BookingServiceImpl->getOwnerBookings end");
        return bookings.stream().filter(Objects::nonNull).map(BookingMapper::mapToBookingDto).toList();
    }

    private User validateUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            String error = String.format("User with ID: %d not found", userId);
            log.warn(error);
            return new NotFoundException(error);
        });
    }

    private Item validateItem(Integer itemId) {
        return itemRepository.findByWithOwner(itemId).orElseThrow(() -> {
            String error = String.format("Item with ID: %d not found", itemId);
            log.warn(error);
            return new NotFoundException(error);
        });
    }

    private boolean isBookingOverlapping(BookingDtoRequest bookingDtoNew) {
        return bookingRepository.existsByItemIdAndStatusAndEndGreaterThanEqualAndStartLessThanEqual(bookingDtoNew.getItemId(), BookingStatus.APPROVED, bookingDtoNew.getStart(), bookingDtoNew.getEnd());
    }

    private List<Booking> getBookingsByState(Integer userId, BookingState state, boolean isBooker) {
        if (userId == null) {
            String error = "User ID must be specified";
            log.warn(error);
            throw new NotFoundException(error);
        }

        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case ALL ->
                    isBooker ? bookingRepository.findByBookerIdOrderByStartDesc(userId) : bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT ->
                    isBooker ? bookingRepository.findByBookerIdAndStatusAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, BookingStatus.APPROVED, now, now) : bookingRepository.findByItemOwnerIdAndStatusAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, BookingStatus.APPROVED, now, now);
            case PAST ->
                    isBooker ? bookingRepository.findByBookerIdAndStatusAndEndBeforeOrderByStartDesc(userId, BookingStatus.APPROVED, now) : bookingRepository.findByItemOwnerIdAndStatusAndEndBeforeOrderByStartDesc(userId, BookingStatus.APPROVED, now);
            case FUTURE ->
                    isBooker ? bookingRepository.findByBookerIdAndStatusAndStartAfterOrderByStartDesc(userId, BookingStatus.APPROVED, now) : bookingRepository.findByItemOwnerIdAndStatusAndStartAfterOrderByStartDesc(userId, BookingStatus.APPROVED, now);
            case WAITING ->
                    isBooker ? bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING) : bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED ->
                    isBooker ? bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED) : bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> List.of();
        };
    }
}