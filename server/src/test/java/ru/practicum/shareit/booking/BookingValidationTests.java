package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BookingValidationTests {

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
    void createBookingWithNullUserId() {
        BookingRequestDto request = formBookingRequestDto();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(request, null));

        assertEquals("User id cannot be null", exception.getMessage());
    }

    @Test
    void createBookingWithNullRequest() {
        Integer userId = 1;

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(null, userId));

        assertEquals("Booking request cannot be null", exception.getMessage());
    }

    @Test
    void createBookingWithNullStartDate() {
        Integer userId = 1;
        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1);
        request.setStart(null);
        request.setEnd(LocalDateTime.now().plusDays(2));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(request, userId));

        assertEquals("Booking start date must be specified", exception.getMessage());
    }

    @Test
    void createBookingWithNullEndDate() {
        Integer userId = 1;
        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(request, userId));

        assertEquals("Booking end date must be specified", exception.getMessage());
    }

    @Test
    void createBookingWithNullItemId() {
        Integer userId = 1;
        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(null);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(request, userId));

        assertEquals("Item ID must be specified", exception.getMessage());
    }

    @Test
    void createBookingWithStartAfterEnd() {
        Integer userId = 1;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1);
        request.setStart(start);
        request.setEnd(end);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(request, userId));

        assertEquals("Start date cannot be after end date", exception.getMessage());
    }

    @Test
    void createBookingWithStartEqualsEnd() {
        Integer userId = 1;
        LocalDateTime dateTime = LocalDateTime.now().plusDays(1);

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1);
        request.setStart(dateTime);
        request.setEnd(dateTime);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(request, userId));

        assertEquals("Start and end dates cannot be the same", exception.getMessage());
    }

    @Test
    void createBookingWithStartInPast() {
        Integer userId = 1;
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1);
        request.setStart(start);
        request.setEnd(end);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(request, userId));

        assertEquals("Start date cannot be in the past", exception.getMessage());
    }

    @Test
    void createBookingWithEndInPast() {
        Integer userId = 1;
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1);
        request.setStart(start);
        request.setEnd(end);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(request, userId));

        assertEquals("Start date cannot be after end date", exception.getMessage());
    }

    private BookingRequestDto formBookingRequestDto() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        return dto;
    }
}