package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository repository;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingMapper mapper;

    @Override
    @Transactional
    public BookingDto create(BookingRequestDto bookingRequestDto, Integer userId) {
        log.info("BookingServiceImpl->create start");
        log.info("bookingRequestDto={}, userId={}", bookingRequestDto, userId);

        validateCreateRequest(bookingRequestDto, userId);
        UserDto booker = getUserById(userId);
        ItemDto item = getItemById(bookingRequestDto.getItemId());

        if (item.getUserId().equals(userId)) {
            throw new ValidationException("Item owner cannot book their own item");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Item with id " + bookingRequestDto.getItemId() + " is busy");
        }

        checkItemAvailability(bookingRequestDto);

        Booking booking = mapper.toEntity(bookingRequestDto);
        booking.setUserId(userId);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = repository.save(booking);
        log.info(">>>new {}", booking);
        log.info(">>>saved {}", savedBooking);

        BookingDto bookingDto = mapper.toDto(savedBooking);
        bookingDto.setBooker(booker);
        bookingDto.setItem(item);

        log.info(">>>dto {}", bookingDto);
        log.info("BookingServiceImpl->create end");
        return bookingDto;
    }

    @Override
    @Transactional
    public BookingDto approve(Integer bookingId, Boolean approved, Integer userId) {
        log.info("BookingServiceImpl->approve start");
        log.info("bookingId={}, approved={}, userId={}", bookingId, approved, userId);

        if (bookingId == null) {
            throw new ValidationException("Booking id cannot be null");
        }
        if (approved == null) {
            throw new ValidationException("Status cannot be null");
        }

        Booking booking = getBookingById(bookingId);

        ItemDto item = getItemById(booking.getItemId());

        if (!item.getUserId().equals(userId)) {
            log.warn("User {} trying to approve someone else's booking {}", userId, bookingId);
            throw new ValidationException("Only the item owner can approve a booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking already has status: " + booking.getStatus());
        }

        BookingStatus newBookingStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newBookingStatus);

        Booking updatedBooking = repository.save(booking);
        log.info("Booking {} status changed to {}", bookingId, newBookingStatus);

        BookingDto bookingDto = mapper.toDto(updatedBooking);
        try {
            bookingDto.setBooker(getUserById(booking.getUserId()));
            bookingDto.setItem(item);
        } catch (Exception e) {
            log.warn("!!!!!Failed to load data for response: {}", e.getMessage());
        }

        log.info("BookingServiceImpl->approve end");
        return bookingDto;
    }

    @Override
    public BookingDto getById(Integer bookingId, Integer userId) {
        log.info("BookingServiceImpl->getById start");
        log.info("bookingId={}, userId={}", bookingId, userId);

        if (bookingId == null) {
            throw new ValidationException("Booking id cannot be null");
        }

        getUserById(userId);

        Booking booking = getBookingById(bookingId);

        ItemDto item = getItemById(booking.getItemId());

        if (!booking.getUserId().equals(userId) && !item.getUserId().equals(userId)) {
            log.warn("User {} trying to fetch someone else's booking {}", userId, bookingId);
            throw new ValidationException("Only author or owner can touch booking");
        }
        BookingDto bookingDto = mapper.toDto(booking);
        bookingDto.setBooker(getUserById(booking.getUserId()));
        bookingDto.setItem(item);

        log.info("BookingServiceImpl->getById end");
        return bookingDto;
    }

    @Override
    public List<BookingDto> getUserBookings(Integer userId, String state, Integer from, Integer size) {
        log.info("BookingServiceImpl->getUserBookings start");
        log.info("userId={}, state={}, from={}, size={}", userId, state, from, size);

        getUserById(userId);

        validatePagination(from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("startDate").descending());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = getBookingsByState(userId, state, now, pageable, false);
        List<BookingDto> result = convertToDtoList(bookings);

        log.info("BookingServiceImpl->getUserBookings end");
        return result;
    }

    @Override
    public List<BookingDto> getOwnerBookings(Integer ownerId, String state, Integer from, Integer size) {
        log.info("BookingServiceImpl->getOwnerBookings start");
        log.info("ownerId={}, state={}, from={}, size={}", ownerId, state, from, size);

        getUserById(ownerId);

        validatePagination(from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("startDate").descending());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = getBookingsByState(ownerId, state, now, pageable, true);
        List<BookingDto> result = convertToDtoList(bookings);

        log.info("BookingServiceImpl->getOwnerBookings end");
        return result;
    }

    private List<Booking> getBookingsByState(Integer userId, String state, LocalDateTime now,
                                             Pageable pageable, boolean isOwner) {
        String upperState = state.toUpperCase();

        if (isOwner) {
            return switch (upperState) {
                case "ALL" -> repository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
                case "CURRENT" -> repository.findCurrentByOwnerId(userId, now, pageable);
                case "PAST" -> repository.findPastByOwnerId(userId, now, pageable);
                case "FUTURE" -> repository.findFutureByOwnerId(userId, now, pageable);
                case "WAITING" -> repository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING.toString(), pageable);
                case "REJECTED" -> repository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED.toString(), pageable);
                default -> {
                    log.error("Unknown booking state: {}", state);
                    throw new ValidationException("Unknown state: " + state);
                }
            };
        } else {
            return switch (upperState) {
                case "ALL" -> repository.findByUserIdOrderByStartDateDesc(userId, pageable);
                case "CURRENT" -> repository.findCurrentByBookerId(userId, now, pageable);
                case "PAST" -> repository.findPastByBookerId(userId, now, pageable);
                case "FUTURE" -> repository.findFutureByBookerId(userId, now, pageable);
                case "WAITING" ->
                        repository.findByUserIdAndStatusOrderByStartDateDesc(userId, BookingStatus.WAITING, pageable);
                case "REJECTED" ->
                        repository.findByUserIdAndStatusOrderByStartDateDesc(userId, BookingStatus.REJECTED, pageable);
                default -> {
                    log.error("Unknown booking state: {}", state);
                    throw new ValidationException("Unknown state: " + state);
                }
            };
        }
    }

    private void validateCreateRequest(BookingRequestDto bookingRequestDto, Integer userId) {
        if (userId == null) {
            throw new ValidationException("User id cannot be null");
        }
        if (bookingRequestDto == null) {
            throw new ValidationException("Booking request cannot be null");
        }
        if (bookingRequestDto.getStart() == null) {
            throw new ValidationException("Booking start date must be specified");
        }
        if (bookingRequestDto.getEnd() == null) {
            throw new ValidationException("Booking end date must be specified");
        }
        if (bookingRequestDto.getItemId() == null) {
            throw new ValidationException("Item ID must be specified");
        }

        LocalDateTime now = LocalDateTime.now();

        if (bookingRequestDto.getStart().isAfter(bookingRequestDto.getEnd())) {
            throw new ValidationException("Start date cannot be after end date");
        }
        if (bookingRequestDto.getStart().equals(bookingRequestDto.getEnd())) {
            throw new ValidationException("Start and end dates cannot be the same");
        }
        if (bookingRequestDto.getStart().isBefore(now)) {
            throw new ValidationException("Start date cannot be in the past");
        }
        if (bookingRequestDto.getEnd().isBefore(now)) {
            throw new ValidationException("End date cannot be in the past");
        }
    }

    private void checkItemAvailability(BookingRequestDto bookingRequestDto) {
        boolean isBooked = repository.existsApprovedBookingsForItemBetweenDates(
                bookingRequestDto.getItemId(),
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd());

        if (isBooked) {
            throw new ValidationException("Item is already booked for the specified dates");
        }
    }

    private void validatePagination(Integer from, Integer size) {
        if (from < 0) {
            throw new ValidationException("'from' cannot be negative");
        }
        if (size <= 0) {
            throw new ValidationException("'size' must be positive");
        }
    }

    private UserDto getUserById(Integer userId) {
        try {
            return userService.getUserById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("User with id " + userId + " not found");
        } catch (Exception e) {
            throw new InternalServerException("Error while checking user: " + e.getMessage());
        }
    }

    private ItemDto getItemById(Integer itemId) {
        try {
            return itemService.getItemById(itemId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Item with id " + itemId + " not found");
        }
    }

    private Booking getBookingById(Integer bookingId) {
        return repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id = " + bookingId + " not found"));
    }

    private List<BookingDto> convertToDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(booking -> {
                    BookingDto dto = mapper.toDto(booking);
                    try {
                        dto.setBooker(getUserById(booking.getUserId()));
                        dto.setItem(getItemById(booking.getItemId()));
                    } catch (Exception e) {
                        log.warn("Failed to load data for booking {}: {}", booking.getId(), e.getMessage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}