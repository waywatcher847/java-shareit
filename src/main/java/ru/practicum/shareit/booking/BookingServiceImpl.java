package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional(readOnly = true)
@Qualifier("BookingServiceImpl")
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                            ItemRepository itemRepository,
                            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BookingDto createBooking(BookingDtoNew bookingDtoNew, Integer bookerId) {
        log.info("BookingServiceImpl->createBooking start");
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
        Booking booking = Booking.builder()
                .start(bookingDtoNew.getStart())
                .end(bookingDtoNew.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        log.info(">>> !!!" + booking);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("BookingServiceImpl->createBooking end");
        return mapToBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto setBookingApproval(Integer bookingId, Integer userId, Boolean approved) {
        log.info("BookingServiceImpl->setBookingApproval start");
        if (approved == null) {
            String error = "must be true or false";
            log.warn(error);
            throw new NotFoundException(error);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User with ID: " + userId + " not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with ID: %d does not exist", bookingId)));

        if (booking.getItem().getOwner().equals(user)) {
            if (booking.getStatus().equals(BookingStatus.WAITING)) {
                boolean hasOverlap = bookingRepository.existsByItemIdAndStatusAndEndGreaterThanEqualAndStartLessThanEqual(
                        booking.getItem().getId(), BookingStatus.APPROVED, booking.getStart(), booking.getEnd());

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

        log.info("BookingServiceImpl->setBookingApproval end");
        return mapToBookingDto(booking);
    }

    @Override
    public BookingDto getBooking(Integer bookingId, Integer userId) {
        log.info("BookingServiceImpl->getBooking start");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with ID: %d does not exist", bookingId)));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            String error = "Not the item owner or the booking author";
            log.warn(error);
            throw new AccessDeniedException(error);
        }

        log.info("BookingServiceImpl->getBooking end");
        return mapToBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookings(Integer bookerId, BookingState bookingState) {
        log.info("BookingServiceImpl->getAllBookings start");
        validateUser(bookerId);
        int size = 10;
        Pageable pageable = PageRequest.of(0, size);

        List<Booking> bookings = getBookingsByState(bookerId, bookingState, pageable, true);

        log.info("BookingServiceImpl->getAllBookings end");
        return bookings.stream()
                .filter(Objects::nonNull)
                .map(BookingServiceImpl::mapToBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Integer ownerId, BookingState bookingState) {
        log.info("BookingServiceImpl->getBookingsByOwner start");
        validateUser(ownerId);
        int size = 10;
        Pageable pageable = PageRequest.of(0, size);

        List<Booking> bookings = getBookingsByState(ownerId, bookingState, pageable, false);

        log.info("BookingServiceImpl->getBookingsByOwner end");
        return bookings.stream()
                .filter(Objects::nonNull)
                .map(BookingServiceImpl::mapToBookingDto)
                .toList();
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

    private boolean isBookingOverlapping(BookingDtoNew bookingDtoNew) {
        return bookingRepository.existsByItemIdAndStatusAndEndGreaterThanEqualAndStartLessThanEqual(
                bookingDtoNew.getItemId(),
                BookingStatus.APPROVED,
                bookingDtoNew.getStart(),
                bookingDtoNew.getEnd());
    }

    private List<Booking> getBookingsByState(Integer userId, BookingState state, Pageable pageable, boolean isBooker) {
        if (userId == null) {
            String error = "User ID must be specified";
            log.warn(error);
            throw new NotFoundException(error);
        }

        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case ALL -> isBooker
                    ? bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable)
                    : bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
            case CURRENT -> isBooker
                    ? bookingRepository.findByBookerIdAndStatusAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, BookingStatus.APPROVED, now, now, pageable)
                    : bookingRepository.findByItemOwnerIdAndStatusAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, BookingStatus.APPROVED, now, now, pageable);
            case PAST -> isBooker
                    ? bookingRepository.findByBookerIdAndStatusAndEndBeforeOrderByStartDesc(userId, BookingStatus.APPROVED, now, pageable)
                    : bookingRepository.findByItemOwnerIdAndStatusAndEndBeforeOrderByStartDesc(userId, BookingStatus.APPROVED, now, pageable);
            case FUTURE -> isBooker
                    ? bookingRepository.findByBookerIdAndStatusAndStartAfterOrderByStartDesc(userId, BookingStatus.APPROVED, now, pageable)
                    : bookingRepository.findByItemOwnerIdAndStatusAndStartAfterOrderByStartDesc(userId, BookingStatus.APPROVED, now, pageable);
            case WAITING -> isBooker
                    ? bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable)
                    : bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable);
            case REJECTED -> isBooker
                    ? bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, pageable)
                    : bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, pageable);
            default -> List.of();
        };
    }

    public static BookingDto mapToBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(booking.getStatus())
                .build();
    }
}