package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.common.booking.BookingDtoRequest;
import ru.practicum.common.booking.BookingState;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(BookingClient.class)
@TestPropertySource(properties = "shareit-server.url=http://localhost:8080")
class BookingClientTests {

    @Autowired
    private BookingClient bookingClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void createBooking_shouldCallCorrectEndpoint() {
        BookingDtoRequest dto = BookingDtoRequest.builder()
                .itemId(1)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        server.expect(requestTo("http://localhost:8080/internal/bookings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = bookingClient.createBooking(dto, 1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void approveBooking_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/bookings/1?approved=true"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = bookingClient.approveBooking(1, true, 1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void getBookingById_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/bookings/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = bookingClient.getBookingById(1, 1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void getUserBookings_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/bookings?state=ALL"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = bookingClient.getUserBookings(1, BookingState.ALL);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void getOwnerBookings_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/bookings/owner?state=CURRENT"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = bookingClient.getOwnerBookings(1, BookingState.CURRENT);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void deleteBooking_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/bookings/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        var response = bookingClient.deleteBooking(1, 1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }
}