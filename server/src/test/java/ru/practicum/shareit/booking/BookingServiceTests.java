package ru.practicum.shareit.booking;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTests {

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

    @Test
    void createBooking() {
        Integer userId = 1;
        Integer itemId = 1;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingRequestDto request = formBookingRequestDto(itemId, start, end);
        UserDto bookerDto = formUserDto(userId, "Booker", "booker@email.ru");
        ItemDto itemDto = formItemDto(itemId, 2, true); // owner id = 2 (не равен userId)

        Booking bookingEntity = formBooking(null, itemId, userId, BookingStatus.WAITING);
        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(1);

        when(userService.getUserById(userId)).thenReturn(bookerDto);
        when(itemService.getItemById(itemId)).thenReturn(itemDto);
        when(bookingRepository.existsApprovedBookingsForItemBetweenDates(eq(itemId), eq(start), eq(end))).thenReturn(false);
        when(mapper.toEntity(any(BookingRequestDto.class))).thenReturn(bookingEntity);
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingEntity);
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedDto);

        BookingDto result = bookingService.create(request, userId);

        assertEquals(expectedDto, result);
    }

    @Test
    void createBookingWithNotExistentUser() {
        Integer userId = 99;
        BookingRequestDto request = formBookingRequestDto(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(userService.getUserById(userId)).thenThrow(new NotFoundException("Internal msg"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(request, userId));
        assertEquals("User with id 99 not found", exception.getMessage());
    }

    @Test
    void createBookingWithNotExistentItem() {
        Integer userId = 1;
        Integer itemId = 99;
        BookingRequestDto request = formBookingRequestDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        UserDto bookerDto = formUserDto(userId, "Booker", "booker@email.ru");

        when(userService.getUserById(userId)).thenReturn(bookerDto);
        when(itemService.getItemById(itemId)).thenThrow(new NotFoundException("Internal msg"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(request, userId));
        assertEquals("Item with id 99 not found", exception.getMessage());
    }

    @Test
    void createBookingWithUnavailableItem() {
        Integer userId = 1;
        Integer itemId = 1;
        BookingRequestDto request = formBookingRequestDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        UserDto bookerDto = formUserDto(userId, "Booker", "booker@email.ru");
        ItemDto itemDto = formItemDto(itemId, 2, false); // unavailable

        when(userService.getUserById(userId)).thenReturn(bookerDto);
        when(itemService.getItemById(itemId)).thenReturn(itemDto);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(request, userId));
        assertEquals("Item with id 1 is busy", exception.getMessage());
    }

    @Test
    void approveBooking() {
        Integer userId = 1; // owner
        Integer bookingId = 1;
        Integer itemId = 1;
        Integer bookerId = 2;

        Booking booking = formBooking(bookingId, itemId, bookerId, BookingStatus.WAITING);
        ItemDto itemDto = formItemDto(itemId, userId, true); // owner id == userId
        UserDto bookerDto = formUserDto(bookerId, "Booker", "booker@email.ru");
        BookingDto expectedDto = new BookingDto();
        expectedDto.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemService.getItemById(itemId)).thenReturn(itemDto);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedDto);
        when(userService.getUserById(bookerId)).thenReturn(bookerDto);

        BookingDto result = bookingService.approve(bookingId, true, userId);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void rejectBooking() {
        Integer userId = 1;
        Integer bookingId = 1;
        Integer itemId = 1;
        Integer bookerId = 2;

        Booking booking = formBooking(bookingId, itemId, bookerId, BookingStatus.WAITING);
        ItemDto itemDto = formItemDto(itemId, userId, true);
        UserDto bookerDto = formUserDto(bookerId, "Booker", "booker@email.ru");
        BookingDto expectedDto = new BookingDto();
        expectedDto.setStatus(BookingStatus.REJECTED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemService.getItemById(itemId)).thenReturn(itemDto);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedDto);
        when(userService.getUserById(bookerId)).thenReturn(bookerDto);

        BookingDto result = bookingService.approve(bookingId, false, userId);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
    }

    @Test
    void approveNotExistentBooking() {
        Integer userId = 1;
        Integer bookingId = 2;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.approve(bookingId, true, userId));
        assertEquals("Booking with id = 2 not found", exception.getMessage());
    }

    @Test
    void approveBookingWithWrongUser() {
        Integer userId = 3; // wrong user
        Integer bookingId = 1;
        Integer itemId = 1;
        Integer bookerId = 2;

        Booking booking = formBooking(bookingId, itemId, bookerId, BookingStatus.WAITING);
        ItemDto itemDto = formItemDto(itemId, 1, true); // owner id = 1 != userId

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemService.getItemById(itemId)).thenReturn(itemDto);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.approve(bookingId, true, userId));
        assertEquals("Only the item owner can approve a booking", exception.getMessage());
    }

    @Test
    void getBookingById() {
        Integer userId = 1; // author
        Integer bookingId = 1;
        Integer itemId = 1;
        Integer bookerId = 1;

        Booking booking = formBooking(bookingId, itemId, bookerId, BookingStatus.WAITING);
        ItemDto itemDto = formItemDto(itemId, 2, true);
        UserDto bookerDto = formUserDto(bookerId, "Booker", "booker@email.ru");
        BookingDto expectedDto = new BookingDto();

        when(userService.getUserById(userId)).thenReturn(bookerDto);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemService.getItemById(itemId)).thenReturn(itemDto);
        when(mapper.toDto(any(Booking.class))).thenReturn(expectedDto);
        when(userService.getUserById(bookerId)).thenReturn(bookerDto);

        BookingDto result = bookingService.getById(bookingId, userId);

        assertEquals(expectedDto, result);
    }

    @Test
    void getNotExistentBookingById() {
        Integer userId = 1;
        Integer bookingId = 7;
        UserDto userDto = formUserDto(userId, "User", "user@email.ru");

        when(userService.getUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getById(bookingId, userId));
        assertEquals("Booking with id = 7 not found", exception.getMessage());
    }

    @Test
    void getAllUsersBookings() {
        Integer bookerId = 2;
        String state = "ALL";
        UserDto userDto = formUserDto(bookerId, "Booker", "booker@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, bookerId, BookingStatus.WAITING));

        when(bookingRepository.findByUserIdOrderByStartDateDesc(eq(bookerId), any(Pageable.class))).thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getUserBookings(bookerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllUsersBookingsWithCurrentStatus() {
        Integer bookerId = 2;
        String state = "CURRENT";
        UserDto userDto = formUserDto(bookerId, "Booker", "booker@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, bookerId, BookingStatus.WAITING));

        when(bookingRepository.findCurrentByBookerId(eq(bookerId), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getUserBookings(bookerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllUsersBookingsWithPastStatus() {
        Integer bookerId = 2;
        String state = "PAST";
        UserDto userDto = formUserDto(bookerId, "Booker", "booker@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, bookerId, BookingStatus.WAITING));

        when(bookingRepository.findPastByBookerId(eq(bookerId), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getUserBookings(bookerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllUsersBookingsWithFutureStatus() {
        Integer bookerId = 2;
        String state = "FUTURE";
        UserDto userDto = formUserDto(bookerId, "Booker", "booker@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, bookerId, BookingStatus.WAITING));

        when(bookingRepository.findFutureByBookerId(eq(bookerId), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getUserBookings(bookerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllUsersBookingsWithWaitingStatus() {
        Integer bookerId = 2;
        String state = "WAITING";
        UserDto userDto = formUserDto(bookerId, "Booker", "booker@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, bookerId, BookingStatus.WAITING));


        when(bookingRepository.findByUserIdAndStatusOrderByStartDateDesc(eq(bookerId), eq(BookingStatus.WAITING), any(Pageable.class)))
                .thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getUserBookings(bookerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllUsersBookingsWithRejectedStatus() {
        Integer bookerId = 2;
        String state = "REJECTED";
        UserDto userDto = formUserDto(bookerId, "Booker", "booker@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, bookerId, BookingStatus.REJECTED));


        when(bookingRepository.findByUserIdAndStatusOrderByStartDateDesc(eq(bookerId), eq(BookingStatus.REJECTED), any(Pageable.class)))
                .thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getUserBookings(bookerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllUsersBookingsWithInvalidStatus() {
        Integer bookerId = 2;
        String state = "INVALID";
        UserDto userDto = formUserDto(bookerId, "Booker", "booker@email.ru");



        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getUserBookings(bookerId, state, 0, 10));
        assertEquals("Unknown state: INVALID", exception.getMessage());
    }

    @Test
    void getAllOwnersBookings() {
        Integer ownerId = 1;
        String state = "ALL";
        UserDto userDto = formUserDto(ownerId, "Owner", "owner@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, 2, BookingStatus.WAITING));

        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(ownerId), any(Pageable.class))).thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getOwnerBookings(ownerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOwnersBookingsWithCurrentStatus() {
        Integer ownerId = 1;
        String state = "CURRENT";
        UserDto userDto = formUserDto(ownerId, "Owner", "owner@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, 2, BookingStatus.WAITING));

        when(bookingRepository.findCurrentByOwnerId(eq(ownerId), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getOwnerBookings(ownerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOwnersBookingsWithPastStatus() {
        Integer ownerId = 1;
        String state = "PAST";
        UserDto userDto = formUserDto(ownerId, "Owner", "owner@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, 2, BookingStatus.WAITING));

        when(bookingRepository.findPastByOwnerId(eq(ownerId), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getOwnerBookings(ownerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOwnersBookingsWithFutureStatus() {
        Integer ownerId = 1;
        String state = "FUTURE";
        UserDto userDto = formUserDto(ownerId, "Owner", "owner@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, 2, BookingStatus.WAITING));

        when(bookingRepository.findFutureByOwnerId(eq(ownerId), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getOwnerBookings(ownerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOwnersBookingsWithWaitingStatus() {
        Integer ownerId = 1;
        String state = "WAITING";
        UserDto userDto = formUserDto(ownerId, "Owner", "owner@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, 2, BookingStatus.WAITING));

        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(eq(ownerId), eq(BookingStatus.WAITING.toString()), any(Pageable.class)))
                .thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getOwnerBookings(ownerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOwnersBookingsWithRejectedStatus() {
        Integer ownerId = 1;
        String state = "REJECTED";
        UserDto userDto = formUserDto(ownerId, "Owner", "owner@email.ru");
        List<Booking> bookings = List.of(formBooking(1, 1, 2, BookingStatus.REJECTED));

        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(eq(ownerId), eq(BookingStatus.REJECTED.toString()), any(Pageable.class)))
                .thenReturn(bookings);
        mockConvertToDtoList();

        List<BookingDto> result = bookingService.getOwnerBookings(ownerId, state, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOwnersBookingsWithInvalidStatus() {
        Integer ownerId = 1;
        String state = "INVALID";
        UserDto userDto = formUserDto(ownerId, "Owner", "owner@email.ru");


        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getOwnerBookings(ownerId, state, 0, 10));
        assertEquals("Unknown state: INVALID", exception.getMessage());
    }

    private void mockConvertToDtoList() {
        when(mapper.toDto(any(Booking.class))).thenReturn(new BookingDto());
        when(userService.getUserById(anyInt())).thenReturn(new UserDto());
        when(itemService.getItemById(anyInt())).thenReturn(new ItemDto());
    }

    private UserDto formUserDto(Integer id, String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }

    private ItemDto formItemDto(Integer id, Integer userId, boolean available) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(id);
        itemDto.setUserId(userId);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(available);
        return itemDto;
    }

    private BookingRequestDto formBookingRequestDto(Integer itemId, LocalDateTime start, LocalDateTime end) {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

    private Booking formBooking(Integer id, Integer itemId, Integer userId, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setItemId(itemId);
        booking.setUserId(userId);
        booking.setStatus(status);
        return booking;
    }
}