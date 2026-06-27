package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.booking.BookingDtoRequest;
import ru.practicum.common.booking.BookingState;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.common.Constants.USER_ID_HEADER;

@WebMvcTest(BookingController.class)
class BookingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createBooking_withValidDates_shouldReturnOk() throws Exception {
        BookingDtoRequest dto = BookingDtoRequest.builder()
                .itemId(1)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        when(bookingClient.createBooking(any(), eq(1))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_withEndBeforeStart_shouldReturnBadRequest() throws Exception {
        BookingDtoRequest dto = BookingDtoRequest.builder()
                .itemId(1)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        BookingDtoRequest dto = BookingDtoRequest.builder()
                .itemId(1)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_withNullItemId_shouldReturnBadRequest() throws Exception {
        BookingDtoRequest dto = BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build(); // itemId = null

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_withNegativeItemId_shouldReturnBadRequest() throws Exception {
        BookingDtoRequest dto = BookingDtoRequest.builder()
                .itemId(-1)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_withStartInPast_shouldReturnBadRequest() throws Exception {
        BookingDtoRequest dto = BookingDtoRequest.builder()
                .itemId(1)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_withEndInPast_shouldReturnBadRequest() throws Exception {
        BookingDtoRequest dto = BookingDtoRequest.builder()
                .itemId(1)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_withNullStart_shouldReturnBadRequest() throws Exception {
        BookingDtoRequest dto = BookingDtoRequest.builder()
                .itemId(1)
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_withNullEnd_shouldReturnBadRequest() throws Exception {
        BookingDtoRequest dto = BookingDtoRequest.builder()
                .itemId(1)
                .start(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_shouldReturnOk() throws Exception {
        when(bookingClient.approveBooking(eq(1), eq(true), eq(1))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserBookings_shouldReturnOk() throws Exception {
        when(bookingClient.getUserBookings(eq(1), any(BookingState.class))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "ALL"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserBookings_withInvalidState_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_shouldReturnOk() throws Exception {
        when(bookingClient.getOwnerBookings(eq(1), any(BookingState.class))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_withInvalidState_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_withoutApprovedParam_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBooking_shouldReturnOk() throws Exception {
        when(bookingClient.deleteBooking(1, 1)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/bookings/1").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void deleteBooking_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isBadRequest());
    }

}