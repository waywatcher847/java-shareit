package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.common.booking.BookingStatus;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        log.info("Gateway: POST /bookings");
        return bookingClient.createBooking(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @PathVariable Integer bookingId,
            @RequestParam Boolean approved) {
        log.info("Gateway: PATCH /bookings/{}", bookingId);
        return bookingClient.approveBooking(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @PathVariable Integer bookingId) {
        log.info("Gateway: GET /bookings/{}", bookingId);
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Gateway: GET /bookings");
        BookingStatus status = BookingStatus.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.getUserBookings(userId, status, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Integer ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Gateway: GET /bookings/owner");
        BookingStatus status = BookingStatus.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.getOwnerBookings(ownerId, status, from, size);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Object> deleteBooking(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @PathVariable Integer bookingId) {
        log.info("Gateway: DELETE /bookings/{}", bookingId);
        return bookingClient.deleteBooking(userId, bookingId);
    }
}