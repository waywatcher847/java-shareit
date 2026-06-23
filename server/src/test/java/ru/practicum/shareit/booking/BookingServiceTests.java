package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @Mock
    private BookingMapper mapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private UserDto testUserDto;
    private ItemDto testItemDto;
    private Booking testBooking;
    private BookingDto expectedBookingDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testUserDto = new UserDto();
        testUserDto.setId(1);
        testUserDto.setName("John");
        testUserDto.setEmail("john@example.com");

        testItemDto = new ItemDto();
        testItemDto.setId(1);
        testItemDto.setName("Drill");
        testItemDto.setDescription("Power drill");
        testItemDto.setAvailable(true);
        testItemDto.setUserId(1);

        testBooking = Booking.builder().id(1).startDate(now.plusDays(1)).endDate(now.plusDays(3)).userId(1).itemId(1).status(BookingStatus.WAITING).build();

        expectedBookingDto = new BookingDto();
        expectedBookingDto.setId(1);
        expectedBookingDto.setStart(testBooking.getStartDate());
        expectedBookingDto.setEnd(testBooking.getEndDate());
        expectedBookingDto.setStatus(testBooking.getStatus());
    }

    @Test
    void bookingService_WhenCreatingBookingWithNonExistentUser_ThrowsNotFoundException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(1);
        requestDto.setStart(now.plusDays(1));
        requestDto.setEnd(now.plusDays(3));

        when(userService.getUserById(99)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () -> bookingService.create(requestDto, 99));
        verify(userService).getUserById(99);
    }

    @Test
    void bookingService_WhenCreatingBookingWithNonExistentItem_ThrowsNotFoundException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(99);
        requestDto.setStart(now.plusDays(1));
        requestDto.setEnd(now.plusDays(3));

        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(itemService.getItemById(99)).thenThrow(new NotFoundException("Item not found"));

        assertThrows(NotFoundException.class, () -> bookingService.create(requestDto, 1));
        verify(userService).getUserById(1);
        verify(itemService).getItemById(99);
    }

    @Test
    void bookingService_WhenCreatingBookingWithUnavailableItem_ThrowsValidationException() {
        testItemDto.setAvailable(false);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(1);
        requestDto.setStart(now.plusDays(1));
        requestDto.setEnd(now.plusDays(3));

        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(itemService.getItemById(1)).thenReturn(testItemDto);

        assertThrows(ValidationException.class, () -> bookingService.create(requestDto, 1));
    }

    @Test
    void bookingService_WhenCreatingBookingForOwnItem_ThrowsValidationException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(1);
        requestDto.setStart(now.plusDays(1));
        requestDto.setEnd(now.plusDays(3));

        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(itemService.getItemById(1)).thenReturn(testItemDto);

        assertThrows(ValidationException.class, () -> bookingService.create(requestDto, 1));
    }

    @Test
    void bookingService_WhenCreatingBookingWithValidData_ReturnsBookingDto() {
        testItemDto.setUserId(2);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(1);
        requestDto.setStart(now.plusDays(1));
        requestDto.setEnd(now.plusDays(3));

        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(itemService.getItemById(1)).thenReturn(testItemDto);
        when(bookingRepository.existsApprovedBookingsForItemBetweenDates(eq(1), any(), any())).thenReturn(false);
        when(mapper.toEntity(any(BookingRequestDto.class))).thenReturn(testBooking);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(1);
            return b;
        });
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedBookingDto);

        BookingDto result = bookingService.create(requestDto, 1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void bookingService_WhenCreatingBookingWithOverlappingDates_ThrowsValidationException() {
        testItemDto.setUserId(2);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(1);
        requestDto.setStart(now.plusDays(1));
        requestDto.setEnd(now.plusDays(3));

        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(itemService.getItemById(1)).thenReturn(testItemDto);
        when(bookingRepository.existsApprovedBookingsForItemBetweenDates(eq(1), any(), any())).thenReturn(true);

        assertThrows(ValidationException.class, () -> bookingService.create(requestDto, 1));
    }

    @Test
    void bookingService_WhenApprovingNonExistentBooking_ThrowsNotFoundException() {
        when(bookingRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.approve(99, true, 1));
    }

    @Test
    void bookingService_WhenApprovingBookingByNonOwner_ThrowsValidationException() {
        testItemDto.setUserId(2);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(testBooking));
        when(itemService.getItemById(1)).thenReturn(testItemDto);

        assertThrows(ValidationException.class, () -> bookingService.approve(1, true, 99));
    }

    @Test
    void bookingService_WhenApprovingAlreadyApprovedBooking_ThrowsValidationException() {
        testBooking.setStatus(BookingStatus.APPROVED);
        testItemDto.setUserId(1);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(testBooking));
        when(itemService.getItemById(1)).thenReturn(testItemDto);

        assertThrows(ValidationException.class, () -> bookingService.approve(1, true, 1));
    }

    @Test
    void bookingService_WhenApprovingBooking_ReturnsApprovedBooking() {
        testItemDto.setUserId(1);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(testBooking));
        when(itemService.getItemById(1)).thenReturn(testItemDto);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDto approvedDto = new BookingDto();
        approvedDto.setId(1);
        approvedDto.setStatus(BookingStatus.APPROVED);
        when(mapper.toDto(any(Booking.class))).thenReturn(approvedDto);

        BookingDto result = bookingService.approve(1, true, 1);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void bookingService_WhenRejectingBooking_ReturnsRejectedBooking() {
        testItemDto.setUserId(1);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(testBooking));
        when(itemService.getItemById(1)).thenReturn(testItemDto);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDto rejectedDto = new BookingDto();
        rejectedDto.setId(1);
        rejectedDto.setStatus(BookingStatus.REJECTED);
        when(mapper.toDto(any(Booking.class))).thenReturn(rejectedDto);

        BookingDto result = bookingService.approve(1, false, 1);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void bookingService_WhenGettingNonExistentBookingById_ThrowsNotFoundException() {
        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(bookingRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getById(99, 1));
    }

    @Test
    void bookingService_WhenGettingBookingByIdAsBooker_ReturnsBooking() {
        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(testBooking));
        when(itemService.getItemById(1)).thenReturn(testItemDto);
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedBookingDto);

        BookingDto result = bookingService.getById(1, 1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void bookingService_WhenGettingBookingsForNonExistentUser_ThrowsNotFoundException() {
        when(userService.getUserById(99)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () -> bookingService.getUserBookings(99, "ALL", 0, 10));
    }

    @Test
    void bookingService_WhenGettingUserBookingsWithStateAll_ReturnsAllBookings() {
        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(bookingRepository.findByUserIdOrderByStartDateDesc(eq(1), any(Pageable.class))).thenReturn(List.of(testBooking));
        when(itemService.getItemById(1)).thenReturn(testItemDto);
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedBookingDto);

        List<BookingDto> result = bookingService.getUserBookings(1, "ALL", 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(bookingRepository).findByUserIdOrderByStartDateDesc(eq(1), any(Pageable.class));
    }

    @Test
    void bookingService_WhenGettingUserBookingsWithStateWaiting_ReturnsWaitingBookings() {
        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(bookingRepository.findByUserIdAndStatusOrderByStartDateDesc(eq(1), eq(BookingStatus.WAITING), any(Pageable.class))).thenReturn(List.of(testBooking));
        when(itemService.getItemById(1)).thenReturn(testItemDto);
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedBookingDto);

        List<BookingDto> result = bookingService.getUserBookings(1, "WAITING", 0, 10);

        assertNotNull(result);
        verify(bookingRepository).findByUserIdAndStatusOrderByStartDateDesc(eq(1), eq(BookingStatus.WAITING), any(Pageable.class));
    }

    @Test
    void bookingService_WhenGettingUserBookingsWithStateCurrent_ReturnsCurrentBookings() {
        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(bookingRepository.findCurrentByBookerId(eq(1), any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(testBooking));
        when(itemService.getItemById(1)).thenReturn(testItemDto);
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedBookingDto);

        List<BookingDto> result = bookingService.getUserBookings(1, "CURRENT", 0, 10);

        assertNotNull(result);
    }

    @Test
    void bookingService_WhenGettingUserBookingsWithStatePast_ReturnsPastBookings() {
        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(bookingRepository.findPastByBookerId(eq(1), any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of());

        List<BookingDto> result = bookingService.getUserBookings(1, "PAST", 0, 10);

        assertNotNull(result);
    }

    @Test
    void bookingService_WhenGettingUserBookingsWithStateFuture_ReturnsFutureBookings() {
        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(bookingRepository.findFutureByBookerId(eq(1), any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(testBooking));
        when(itemService.getItemById(1)).thenReturn(testItemDto);
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedBookingDto);

        List<BookingDto> result = bookingService.getUserBookings(1, "FUTURE", 0, 10);

        assertNotNull(result);
    }

    @Test
    void bookingService_WhenGettingOwnerBookingsForNonExistentOwner_ThrowsNotFoundException() {
        when(userService.getUserById(99)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(99, "ALL", 0, 10));
    }

    @Test
    void bookingService_WhenGettingOwnerBookingsWithStateAll_ReturnsAllOwnerBookings() {
        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(1), any(Pageable.class))).thenReturn(List.of(testBooking));
        when(itemService.getItemById(1)).thenReturn(testItemDto);
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedBookingDto);

        List<BookingDto> result = bookingService.getOwnerBookings(1, "ALL", 0, 10);

        assertNotNull(result);
    }

    @Test
    void bookingService_WhenGettingOwnerBookingsWithStateRejected_ReturnsRejectedOwnerBookings() {
        when(userService.getUserById(1)).thenReturn(testUserDto);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(eq(1), eq("REJECTED"), any(Pageable.class))).thenReturn(List.of());

        List<BookingDto> result = bookingService.getOwnerBookings(1, "REJECTED", 0, 10);

        assertNotNull(result);
    }
}