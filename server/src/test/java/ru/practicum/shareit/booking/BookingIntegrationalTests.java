package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingIntegrationalTests {
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void bookingService_WhenGettingNonExistentBooking_ThrowsNotFoundException() {
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner2");
        ownerDto.setEmail("owner2@email.ru");
        UserDto owner = userService.create(ownerDto);

        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker2");
        bookerDto.setEmail("booker2@email.ru");
        UserDto booker = userService.create(bookerDto);

        ItemDto availableItemDto = new ItemDto();
        availableItemDto.setName("Item2");
        availableItemDto.setDescription("Description2");
        availableItemDto.setAvailable(true);
        ItemDto item = itemService.create(availableItemDto, owner.getId());

        LocalDateTime start = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.SECONDS);

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(start);
        bookingRequestDto.setEnd(end);

        BookingDto createdBooking = bookingService.create(bookingRequestDto, booker.getId());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getById(createdBooking.getId() + 1, booker.getId()));

        assertEquals("Booking with id = " + (createdBooking.getId() + 1) + " not found", exception.getMessage());
    }

    @Test
    void bookingService_WhenValidRequest_CreatesAndRetrievesBooking() {
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner1");
        ownerDto.setEmail("owner1@email.ru");
        UserDto owner = userService.create(ownerDto);

        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker1");
        bookerDto.setEmail("booker1@email.ru");
        UserDto booker = userService.create(bookerDto);

        ItemDto availableItemDto = new ItemDto();
        availableItemDto.setName("Item1");
        availableItemDto.setDescription("Description1");
        availableItemDto.setAvailable(true);
        ItemDto item = itemService.create(availableItemDto, owner.getId());

        LocalDateTime start = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.SECONDS);

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(start);
        bookingRequestDto.setEnd(end);

        BookingDto createdBooking = bookingService.create(bookingRequestDto, booker.getId());

        BookingDto retrievedBooking = bookingService.getById(createdBooking.getId(), owner.getId());

        assertEquals(createdBooking.getId(), retrievedBooking.getId());
        assertEquals(item.getId(), retrievedBooking.getItem().getId());
        assertEquals(booker.getId(), retrievedBooking.getBooker().getId());
        assertEquals(start, retrievedBooking.getStart());
        assertEquals(end, retrievedBooking.getEnd());
        assertEquals(BookingStatus.WAITING, retrievedBooking.getStatus());
    }

    @Test
    void bookingService_WhenCreatingOrGettingWithInvalidData_ThrowsExceptions() {
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner4");
        ownerDto.setEmail("owner4@email.ru");
        UserDto owner = userService.create(ownerDto);

        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker4");
        bookerDto.setEmail("booker4@email.ru");
        UserDto booker = userService.create(bookerDto);

        ItemDto availableItemDto = new ItemDto();
        availableItemDto.setName("Item4");
        availableItemDto.setDescription("Description4");
        availableItemDto.setAvailable(true);
        ItemDto availableItem = itemService.create(availableItemDto, owner.getId());

        ItemDto unavailableItemDto = new ItemDto();
        unavailableItemDto.setName("Item5");
        unavailableItemDto.setDescription("Description5");
        unavailableItemDto.setAvailable(false);
        ItemDto unavailableItem = itemService.create(unavailableItemDto, owner.getId());

        LocalDateTime start = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.SECONDS);

        BookingRequestDto bookingDto = new BookingRequestDto();
        bookingDto.setItemId(availableItem.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());

        BookingRequestDto bookingNotExistentItemDto = new BookingRequestDto();
        bookingNotExistentItemDto.setItemId(unavailableItem.getId() + 1);
        bookingNotExistentItemDto.setStart(start);
        bookingNotExistentItemDto.setEnd(end);

        BookingRequestDto bookingUnavailableItem = new BookingRequestDto();
        bookingUnavailableItem.setItemId(unavailableItem.getId());
        bookingUnavailableItem.setStart(start);
        bookingUnavailableItem.setEnd(end);

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingDto, booker.getId() + 1));
        assertEquals("User with id " + (booker.getId() + 1) + " not found", exception.getMessage());
        final NotFoundException exception2 = assertThrows(NotFoundException.class,
                () -> bookingService.getById(createdBooking.getId() + 1, booker.getId() + 1));
        assertEquals("User with id " + (booker.getId() + 1) + " not found", exception2.getMessage());
        final NotFoundException exception3 = assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingNotExistentItemDto, booker.getId()));
        assertEquals("Item with id " + bookingNotExistentItemDto.getItemId() + " not found", exception3.getMessage());

        final NotFoundException exception4 = assertThrows(NotFoundException.class,
                () -> bookingService.getById(createdBooking.getId() + 1, booker.getId()));
        assertEquals("Booking with id = " + (createdBooking.getId() + 1) + " not found", exception4.getMessage());

        final ValidationException exception5 = assertThrows(ValidationException.class,
                () -> bookingService.create(bookingUnavailableItem, booker.getId()));
        assertEquals("Item with id " + unavailableItem.getId() + " is busy", exception5.getMessage());

        final NotFoundException exception6 = assertThrows(NotFoundException.class,
                () -> bookingService.getById(createdBooking.getId() + 1, booker.getId()));
        assertEquals("Booking with id = " + (createdBooking.getId() + 1) + " not found", exception6.getMessage());
    }

    @Test
    void bookingService_WhenGettingBookingByWrongUser_ThrowsValidationException() {
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner3");
        ownerDto.setEmail("owner3@email.ru");
        UserDto owner = userService.create(ownerDto);

        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker3");
        bookerDto.setEmail("booker3@email.ru");
        UserDto booker = userService.create(bookerDto);

        UserDto otherUserDto = new UserDto();
        otherUserDto.setName("OtherUser1");
        otherUserDto.setEmail("other_user1@email.ru");
        UserDto notOwnerOrBookerUser = userService.create(otherUserDto);

        ItemDto availableItemDto = new ItemDto();
        availableItemDto.setName("Item3");
        availableItemDto.setDescription("Description3");
        availableItemDto.setAvailable(true);
        ItemDto item = itemService.create(availableItemDto, owner.getId());

        LocalDateTime start = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.SECONDS);

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(start);
        bookingRequestDto.setEnd(end);

        BookingDto createdBooking = bookingService.create(bookingRequestDto, booker.getId());

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getById(createdBooking.getId(), notOwnerOrBookerUser.getId()));

        assertEquals("Only author or owner can touch booking", exception.getMessage());
    }
}