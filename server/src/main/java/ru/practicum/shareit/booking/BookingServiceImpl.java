package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingDtoRequest;
import ru.practicum.common.booking.BookingState;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.validation.ValidationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ValidationService validationService;

    @Override
    @Transactional
    public BookingDto create(BookingDtoRequest bookingDtoNew, Integer bookerId) {
        log.info("BookingServiceImpl->create start, bookingDtoNew={}, bookerId={}", bookingDtoNew, bookerId);

        User booker = validationService.validateUserExists(bookerId);
        Item item = validationService.validateItemExistsWithOwner(bookingDtoNew.getItemId());

        if (booker.equals(item.getOwner())) throw new ConflictException("Item owner cannot book the item ");
        if (!item.getAvailable()) throw new IllegalArgumentException("Item is unavailable");
        if (isBookingOverlapping(bookingDtoNew)) throw new ConflictException("item is already booked");

        Booking booking = Booking.builder()
                .start(bookingDtoNew.getStart())
                .end(bookingDtoNew.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        BookingDto result = BookingMapper.mapToBookingDto(savedBooking);

        log.info("BookingServiceImpl->create end, result={}", result);
        return result;
    }

    @Override
    @Transactional
    public BookingDto approve(Integer bookingId, Integer userId, Boolean approved) {
        log.info("BookingServiceImpl->approve start, bookingId={}, userId={}, approved={}", bookingId, userId, approved);

        User user = validationService.validateUserExists(userId);
        Booking booking = validationService.validateBookingExists(bookingId);

        if (booking.getItem().getOwner().equals(user)) {
            if (booking.getStatus().equals(BookingStatus.WAITING)) {
                boolean hasOverlap =
                        bookingRepository.existsByItemIdAndStatusAndEndGreaterThanEqualAndStartLessThanEqual(
                                booking.getItem()
                                        .getId(),
                                BookingStatus.APPROVED,
                                booking.getStart(),
                                booking.getEnd());

                if (!hasOverlap) {
                    booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
                    bookingRepository.save(booking);
                } else {
                    if (approved) {
                        String error = String.format("the item %d is already booked ", booking.getItem().getId());
                        log.error(error);
                        throw new ConflictException(error);
                    } else {
                        booking.setStatus(BookingStatus.REJECTED);
                        bookingRepository.save(booking);
                    }
                }
            } else {
                String error = "Wrong status";
                log.error(error);
                throw new ConflictException(error);
            }
        } else {
            String error = String.format("user with ID: %d is not the item owner", userId);
            log.warn(error);
            throw new ConflictException(error);
        }

        BookingDto result = BookingMapper.mapToBookingDto(booking);
        log.info("BookingServiceImpl->approve end, result={}", result);
        return result;
    }

    @Override
    public BookingDto getById(Integer bookingId, Integer userId) {
        log.info("BookingServiceImpl->getById start, bookingId={}, userId={}", bookingId, userId);
        Booking booking = validationService.validateBookingExists(bookingId);

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) throw new ConflictException("Not the item owner or the booking author");

        BookingDto result = BookingMapper.mapToBookingDto(booking);
        log.info("BookingServiceImpl->getById end, result={}", result);
        return result;
    }

    @Override
    public List<BookingDto> getUserBookings(Integer bookerId, BookingState bookingState) {
        log.info("BookingServiceImpl->getUserBookings start, bookerId={}, bookingState={}", bookerId, bookingState);
        validationService.validateUserExists(bookerId);

        List<Booking> bookings = getBookingsByState(bookerId, bookingState, true);
        List<BookingDto> result = bookings.stream().filter(Objects::nonNull).map(BookingMapper::mapToBookingDto).toList();

        log.info("BookingServiceImpl->getUserBookings end, result size={}", result.size());
        return result;
    }

    @Override
    public List<BookingDto> getOwnerBookings(Integer ownerId, BookingState bookingState) {
        log.info("BookingServiceImpl->getOwnerBookings start, ownerId={}, bookingState={}", ownerId, bookingState);
        validationService.validateUserExists(ownerId);

        List<Booking> bookings = getBookingsByState(ownerId, bookingState, false);
        List<BookingDto> result = bookings.stream().filter(Objects::nonNull).map(BookingMapper::mapToBookingDto).toList();

        log.info("BookingServiceImpl->getOwnerBookings end, result size={}", result.size());
        return result;
    }

    private boolean isBookingOverlapping(BookingDtoRequest bookingDtoNew) {
        log.info("BookingServiceImpl->isBookingOverlapping start, bookingDtoNew={}", bookingDtoNew);
        boolean result = bookingRepository.existsByItemIdAndStatusAndEndGreaterThanEqualAndStartLessThanEqual(
                bookingDtoNew.getItemId(), BookingStatus.APPROVED, bookingDtoNew.getStart(), bookingDtoNew.getEnd());
        log.info("BookingServiceImpl->isBookingOverlapping end, result={}", result);
        return result;
    }

    private List<Booking> getBookingsByState(Integer userId, BookingState state, boolean isBooker) {
        log.info("BookingServiceImpl->getBookingsByState start, userId={}, state={}, isBooker={}", userId, state, isBooker);

        LocalDateTime now = LocalDateTime.now();

        List<Booking> result = switch (state) {
            case ALL -> isBooker ? bookingRepository.findByBookerIdOrderByStartDesc(userId)
                    : bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT ->
                    isBooker ? bookingRepository.findByBookerIdAndStatusAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, BookingStatus.APPROVED, now, now)
                            : bookingRepository.findByItemOwnerIdAndStatusAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, BookingStatus.APPROVED, now, now);
            case PAST ->
                    isBooker ? bookingRepository.findByBookerIdAndStatusAndEndBeforeOrderByStartDesc(userId, BookingStatus.APPROVED, now)
                            : bookingRepository.findByItemOwnerIdAndStatusAndEndBeforeOrderByStartDesc(userId, BookingStatus.APPROVED, now);
            case FUTURE ->
                    isBooker ? bookingRepository.findByBookerIdAndStatusAndStartAfterOrderByStartDesc(userId, BookingStatus.APPROVED, now)
                            : bookingRepository.findByItemOwnerIdAndStatusAndStartAfterOrderByStartDesc(userId, BookingStatus.APPROVED, now);
            case WAITING ->
                    isBooker ? bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING)
                            : bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED ->
                    isBooker ? bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED)
                            : bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> List.of();
        };
        log.info("BookingServiceImpl->getBookingsByState end, result size={}", result.size());
        return result;
    }
}