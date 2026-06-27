package ru.practicum.shareit.booking;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.common.booking.BookingDtoRequest;
import ru.practicum.common.booking.BookingState;
import ru.practicum.shareit.client.BaseClient;

@Slf4j
@Service
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/internal/bookings"; // без этого мок тесты не работают

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createBooking(BookingDtoRequest bookingDtoRequest, Integer userId) {
        log.info("createBooking userId={}, data: {}", userId, bookingDtoRequest);
        return post("", userId, bookingDtoRequest);
    }

    public ResponseEntity<Object> approveBooking(Integer bookingId, Boolean approved, Integer userId) {
        log.info("approveBooking approved={} userId={}, bookingId: {}", approved, userId, bookingId);
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getBookingById(Integer bookingId, Integer userId) {
        log.info("getBookingById bookingId={} userId={}", bookingId, userId);
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getUserBookings(Integer userId, BookingState state) {
        Map<String, Object> params = Map.of("state", state);
        log.info("getUserBookings userId={}, state: {}", userId, state);
        return get("?state={state}", userId, params);
    }

    public ResponseEntity<Object> getOwnerBookings(Integer ownerId, BookingState state) {
        Map<String, Object> params = Map.of("state", state);
        log.info("getOwnerBookings ownerId={}, state: {}", ownerId, state);
        return get("/owner?state={state}", ownerId, params);
    }

    public ResponseEntity<Object> deleteBooking(Integer userId, Integer bookingId) {
        log.info("deleteBooking userId={} bookingId={}", userId, userId, bookingId);
        return delete("/" + bookingId, userId);
    }
}