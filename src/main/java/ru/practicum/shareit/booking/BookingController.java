package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoNew;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.constants.Constants;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(@Qualifier("BookingServiceImpl") BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable("bookingId") Integer bookingId,
                                         @RequestHeader(Constants.USER_ID_HEADER) Integer userId) {
        log.info("GET /bookings/" + bookingId);
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllBookings(@RequestHeader(Constants.USER_ID_HEADER) Integer bookerId,
                                                   @RequestParam(value = "state", defaultValue = "ALL") String state) {
        log.info("GET /bookings");
        return bookingService.getAllBookings(bookerId, BookingState.from(state));
    }

    @PostMapping
    public BookingDto createBooking(@Valid @RequestBody BookingDtoNew bookingDtoNew,
                                            @RequestHeader(Constants.USER_ID_HEADER) Integer booker) {
        log.info("POST /bookings");
        return bookingService.createBooking(bookingDtoNew, booker);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto setBookingApproval(@PathVariable("bookingId") Integer bookingId,
                                                 @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
                                                 @RequestParam(value = "approved") Boolean approved) {
        log.info("PATCH /bookings/" + bookingId);
        return bookingService.setBookingApproval(bookingId, userId, approved);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(@RequestHeader(Constants.USER_ID_HEADER) Integer ownerId,
                                                       @RequestParam(value = "state", defaultValue = "ALL") String state) {
        log.info("GET /bookings/owner");
        return bookingService.getBookingsByOwner(ownerId, BookingState.from(state));
    }
}