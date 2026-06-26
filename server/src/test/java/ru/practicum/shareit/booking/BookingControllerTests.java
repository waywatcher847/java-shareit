package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingDtoRequest;
import ru.practicum.common.booking.BookingState;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.common.Constants.USER_ID_HEADER;

@WebMvcTest(BookingController.class)
class BookingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_shouldReturn200() throws Exception {
        BookingDtoRequest request = new BookingDtoRequest();
        request.setItemId(1);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1);

        when(bookingService.create(any(BookingDtoRequest.class), eq(1))).thenReturn(bookingDto);

        mockMvc.perform(post("/internal/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createBooking_endBeforeStart_shouldReturn400() throws Exception {
        BookingDtoRequest request = new BookingDtoRequest();
        request.setItemId(1);
        request.setStart(LocalDateTime.now().plusDays(2));
        request.setEnd(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/internal/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_shouldReturn200() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1);

        when(bookingService.approve(eq(1), eq(2), eq(true))).thenReturn(bookingDto);

        mockMvc.perform(patch("/internal/bookings/1")
                        .header(USER_ID_HEADER, 2)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }


    @Test
    void getBookingById_shouldReturn200() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(5);

        when(bookingService.getById(eq(5), eq(1))).thenReturn(bookingDto);

        mockMvc.perform(get("/internal/bookings/5")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getBookingById_withoutUserIdHeader_shouldReturn400() throws Exception {
        mockMvc.perform(get("/internal/bookings/5"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getUserBookings_withDefaultState_shouldReturn200() throws Exception {
        BookingDto b1 = new BookingDto();
        b1.setId(1);
        BookingDto b2 = new BookingDto();
        b2.setId(2);

        when(bookingService.getUserBookings(eq(1), eq(BookingState.ALL)))
                .thenReturn(List.of(b1, b2));

        mockMvc.perform(get("/internal/bookings")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getUserBookings_withExplicitState_shouldReturn200() throws Exception {
        when(bookingService.getUserBookings(eq(1), eq(BookingState.WAITING)))
                .thenReturn(List.of());

        mockMvc.perform(get("/internal/bookings")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUserBookings_withInvalidState_shouldReturn400() throws Exception {
        mockMvc.perform(get("/internal/bookings")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getOwnerBookings_shouldReturn200() throws Exception {
        BookingDto booking = new BookingDto();
        booking.setId(10);

        when(bookingService.getOwnerBookings(eq(2), eq(BookingState.ALL)))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/internal/bookings/owner")
                        .header(USER_ID_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void getOwnerBookings_withStatePast_shouldReturn200() throws Exception {
        when(bookingService.getOwnerBookings(eq(2), eq(BookingState.PAST)))
                .thenReturn(List.of());

        mockMvc.perform(get("/internal/bookings/owner")
                        .header(USER_ID_HEADER, 2)
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getOwnerBookings_withoutUserIdHeader_shouldReturn400() throws Exception {
        mockMvc.perform(get("/internal/bookings/owner"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_notFound_shouldReturn404() throws Exception {
        when(bookingService.approve(eq(999), eq(1), eq(true)))
                .thenThrow(new NotFoundException("Booking with id=999 not found"));

        mockMvc.perform(patch("/internal/bookings/999")
                        .header(USER_ID_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_itemNotFound_shouldReturn404() throws Exception {
        BookingDtoRequest request = new BookingDtoRequest();
        request.setItemId(999);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingService.create(any(BookingDtoRequest.class), eq(1)))
                .thenThrow(new NotFoundException("Item with id=999 not found"));

        mockMvc.perform(post("/internal/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}