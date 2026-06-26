package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import ru.practicum.common.Constants;
import ru.practicum.common.booking.BookingDtoRequest;
import ru.practicum.common.booking.BookingState;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @Valid @RequestBody BookingDtoRequest bookingDtoRequest) {
        log.info("POST /bookings");
        return bookingClient.createBooking(bookingDtoRequest, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @PathVariable Integer bookingId,
            @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{}", bookingId);
        return bookingClient.approveBooking(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @PathVariable Integer bookingId) {
        log.info("GET /bookings/{}", bookingId);
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @RequestParam(name = "state", defaultValue = Constants.DEFAULT_STATE) BookingState state) {
        log.info("Get /bookings?state={}", state);
        return bookingClient.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @RequestHeader(Constants.USER_ID_HEADER) Integer ownerId,
            @RequestParam(name = "state", defaultValue = Constants.DEFAULT_STATE) BookingState state) {
        log.info("GET /bookings/owner?state={}", state);
        return bookingClient.getOwnerBookings(ownerId, state);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Object> deleteBooking(
            @RequestHeader(Constants.USER_ID_HEADER) Integer userId,
            @PathVariable Integer bookingId) {
        log.info("DELETE /bookings/{}", bookingId);
        return bookingClient.deleteBooking(userId, bookingId);
    }
}