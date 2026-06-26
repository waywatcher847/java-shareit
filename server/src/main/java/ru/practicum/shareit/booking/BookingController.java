package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.Constants;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingDtoRequest;
import ru.practicum.common.booking.BookingState;

import java.util.List;

//server
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/internal/bookings") // без этого мок тесты не работают
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BookingDto> createBooking(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @Valid @RequestBody BookingDtoRequest bookingDtoRequest) {
        log.info("Server: POST /bookings");
        return ResponseEntity.ok(bookingService.create(bookingDtoRequest, userId));
    }

    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BookingDto> approveBooking(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @PathVariable Integer bookingId,
            @RequestParam Boolean approved) {
        log.info("Server: PATCH /bookings/{}", bookingId);
        return ResponseEntity.ok(bookingService.approve(bookingId, userId, approved));
    }

    @GetMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BookingDto> getBookingById(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @PathVariable Integer bookingId) {
        log.info("Server: GET /bookings/{}", bookingId);
        return ResponseEntity.ok(bookingService.getById(bookingId, userId));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BookingDto>> getUserBookings(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @RequestParam(name = "state", defaultValue = Constants.DEFAULT_STATE) BookingState state) {
        log.info("Server: GET /bookings");
        return ResponseEntity.ok(bookingService.getUserBookings(userId, state));
    }

    @GetMapping("/owner")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BookingDto>> getOwnerBookings(
            @RequestHeader(Constants.USER_ID_HEADER) Integer ownerId,
            @RequestParam(name = "state", defaultValue = Constants.DEFAULT_STATE) BookingState state) {
        log.info("Server: GET /bookings/owner");
        return ResponseEntity.ok(bookingService.getOwnerBookings(ownerId, state));
    }
}