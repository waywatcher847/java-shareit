package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTests {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void CreateBooking_NullItemId_ReturnsBadRequest() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingDto = new BookingRequestDto();
        bookingDto.setStart(now.plusHours(1).truncatedTo(ChronoUnit.SECONDS));
        bookingDto.setEnd(now.plusHours(2).truncatedTo(ChronoUnit.SECONDS));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void CreateBooking_ValidRequest_ReturnsOk() throws Exception {
        Integer userId = 1;

        LocalDateTime now = LocalDateTime.now();

        BookingRequestDto bookingDto = new BookingRequestDto();
        bookingDto.setItemId(1);
        bookingDto.setStart(now.plusHours(1).truncatedTo(ChronoUnit.SECONDS));
        bookingDto.setEnd(now.plusHours(2).truncatedTo(ChronoUnit.SECONDS));

        when(bookingClient.createBooking(bookingDto, userId))
                .thenReturn(ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(bookingDto));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId", is(bookingDto.getItemId())))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().truncatedTo(ChronoUnit.SECONDS).toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().truncatedTo(ChronoUnit.SECONDS).toString())));
    }

    @Test
    void CreateBooking_PastStartDate_ReturnsBadRequest() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingDto = new BookingRequestDto();
        bookingDto.setItemId(1);
        bookingDto.setStart(now.minusSeconds(1).truncatedTo(ChronoUnit.SECONDS));
        bookingDto.setEnd(now.plusHours(2).truncatedTo(ChronoUnit.SECONDS));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void CreateBooking_NonPositiveItemId_ReturnsBadRequest() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingDtoWithZeroItemId = new BookingRequestDto();
        bookingDtoWithZeroItemId.setItemId(0);
        bookingDtoWithZeroItemId.setStart(now.plusHours(1).truncatedTo(ChronoUnit.SECONDS));
        bookingDtoWithZeroItemId.setEnd(now.plusHours(2).truncatedTo(ChronoUnit.SECONDS));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoWithZeroItemId)))
                .andExpect(status().isBadRequest());

        BookingRequestDto bookingDtoWithNegativeItemId = new BookingRequestDto();
        bookingDtoWithNegativeItemId.setItemId(-1);
        bookingDtoWithNegativeItemId.setStart(now.plusHours(1).truncatedTo(ChronoUnit.SECONDS));
        bookingDtoWithNegativeItemId.setEnd(now.plusHours(2).truncatedTo(ChronoUnit.SECONDS));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoWithNegativeItemId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void CreateBooking_NullEndDate_ReturnsBadRequest() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingDto = new BookingRequestDto();
        bookingDto.setItemId(1);
        bookingDto.setStart(now.plusHours(1).truncatedTo(ChronoUnit.SECONDS));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void CreateBooking_NullStartDate_ReturnsBadRequest() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingDto = new BookingRequestDto();
        bookingDto.setItemId(1);
        bookingDto.setEnd(now.plusHours(2).truncatedTo(ChronoUnit.SECONDS));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void CreateBooking_StartEqualsEnd_ReturnsBadRequest() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingDto = new BookingRequestDto();
        bookingDto.setItemId(1);
        bookingDto.setStart(now.plusHours(1).truncatedTo(ChronoUnit.SECONDS));
        bookingDto.setEnd(now.plusHours(1).truncatedTo(ChronoUnit.SECONDS));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void CreateBooking_StartAfterEnd_ReturnsBadRequest() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingDto = new BookingRequestDto();
        bookingDto.setItemId(1);
        bookingDto.setStart(now.plusHours(1).truncatedTo(ChronoUnit.SECONDS));
        bookingDto.setEnd(now.plusHours(1).minusSeconds(1).truncatedTo(ChronoUnit.SECONDS));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GetBooking_ValidRequest_ReturnsOk() throws Exception {
        Integer bookingId = 1;
        boolean approved = true;

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void ApproveBooking_ValidRequest_ReturnsOk() throws Exception {
        Integer bookingId = 1;
        boolean approved = true;

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk());
    }

    @Test
    void CreateBooking_MissingHeader_ReturnsBadRequest() throws Exception {
        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GetOwnersBookings_ValidRequest_ReturnsOk() throws Exception {
        Integer bookingId = 1;

        mvc.perform(get("/bookings/owner", bookingId)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }
}