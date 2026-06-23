package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingRequestDto;

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
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        log.info("Server: POST /bookings");
        return ResponseEntity.ok(bookingService.create(bookingRequestDto, userId));
    }

    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BookingDto> approveBooking(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @PathVariable Integer bookingId,
            @RequestParam Boolean approved) {
        log.info("Server: PATCH /bookings/{}", bookingId);
        return ResponseEntity.ok(bookingService.approve(bookingId, approved, userId));
    }

    @GetMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BookingDto> getBookingById(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @PathVariable Integer bookingId) {
        log.info("Server: GET /bookings/{}", bookingId);
        return ResponseEntity.ok(bookingService.getById(bookingId, userId));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BookingDto>> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Integer userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Server: GET /bookings");
        return ResponseEntity.ok(bookingService.getUserBookings(userId, state, from, size));
    }

    @GetMapping("/owner")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BookingDto>> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Integer ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Server: GET /bookings/owner");
        return ResponseEntity.ok(bookingService.getOwnerBookings(ownerId, state, from, size));
    }
}