package ru.practicum.shareit.booking;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.shareit.client.BaseClient;

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

    public ResponseEntity<Object> createBooking(BookingRequestDto bookingRequestDto, Integer userId) {
        return post("", userId, bookingRequestDto);
    }

    public ResponseEntity<Object> approveBooking(Integer bookingId, Boolean approved, Integer userId) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getBookingById(Integer bookingId, Integer userId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getUserBookings(Integer userId, BookingStatus state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(Integer ownerId, BookingStatus state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", ownerId, parameters);
    }

    public ResponseEntity<Object> deleteBooking(Integer userId, Integer bookingId) {
        return delete("/" + bookingId, userId);
    }
}