package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
public class BookingControllerTests {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingService bookingService;
    @MockBean
    private ItemService itemService;
    @MockBean
    private UserService userService;
    @MockBean
    private ItemRequestService requestService;
    @MockBean
    private ItemMapper itemMapper;
    @MockBean
    private UserMapper userMapper;
    @MockBean
    private BookingMapper bookingMapper;
    @MockBean
    private ItemRequestMapper itemRequestMapper;


    private BookingRequestDto request;
    private BookingDto response;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime start = now.plusDays(1);
        LocalDateTime end = now.plusDays(2);

        request = new BookingRequestDto();
        request.setItemId(1);
        request.setStart(start);
        request.setEnd(end);

        UserDto owner = new UserDto();
        owner.setId(1);
        owner.setName("Owner");
        owner.setEmail("owner@email.ru");

        UserDto booker = new UserDto();
        booker.setId(2);
        booker.setName("Booker");
        booker.setEmail("booker@email.ru");

        ItemDto item = new ItemDto();
        item.setId(1);
        item.setUserId(owner.getId());
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);

        response = new BookingDto();
        response.setId(1);
        response.setItem(item);
        response.setBooker(booker);
        response.setStart(start);
        response.setEnd(end);
        response.setStatus(BookingStatus.WAITING);
    }

    @Test
    void bookingController_WhenCreatingBooking_ReturnsOkStatus() throws Exception {
        Integer userId = 1;

        when(bookingService.create(any(BookingRequestDto.class), eq(userId)))
                .thenReturn(response);

        mvc.perform(post("/internal/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.item").exists())
                .andExpect(jsonPath("$.booker").exists())
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists())
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    void bookingController_WhenGettingBooking_ReturnsOkStatus() throws Exception {
        Integer userId = 1;
        Integer bookingId = 1;

        when(bookingService.getById(bookingId, userId))
                .thenReturn(response);

        mvc.perform(get("/internal/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.item").exists())
                .andExpect(jsonPath("$.booker").exists())
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists())
                .andExpect(jsonPath("$.status", is("WAITING")));
    }


    @Test
    void bookingController_WhenApprovingBooking_ReturnsOkStatus() throws Exception {
        Integer userId = 1;
        Integer bookingId = 1;
        boolean state = true;

        response.setStatus(BookingStatus.APPROVED);

        when(bookingService.approve(bookingId, state, userId))
                .thenReturn(response);

        mvc.perform(patch("/internal/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.item").exists())
                .andExpect(jsonPath("$.booker").exists())
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void bookingController_WhenGettingOwnersBookings_ReturnsOkStatus() throws Exception {
        Integer userId = 1;
        String state = "WAITING";

        when(bookingService.getOwnerBookings(userId, state, 0, 10))
                .thenReturn(List.of(response));

        mvc.perform(get("/internal/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[*].id").exists())
                .andExpect(jsonPath("$.[*].item").exists())
                .andExpect(jsonPath("$.[*].booker").exists())
                .andExpect(jsonPath("$.[*].start").exists())
                .andExpect(jsonPath("$.[*].end").exists())
                .andExpect(jsonPath("$.[*].status", Matchers.everyItem(is("WAITING"))));
    }

    @Test
    void bookingController_WhenGettingUsersBookings_ReturnsOkStatus() throws Exception {
        Integer userId = 2;
        String state = "WAITING";

        when(bookingService.getUserBookings(userId, state, 0, 10))
                .thenReturn(List.of(response));

        mvc.perform(get("/internal/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[*].id").exists())
                .andExpect(jsonPath("$.[*].item").exists())
                .andExpect(jsonPath("$.[*].booker").exists())
                .andExpect(jsonPath("$.[*].start").exists())
                .andExpect(jsonPath("$.[*].end").exists())
                .andExpect(jsonPath("$.[*].status", Matchers.everyItem(is("WAITING"))));
    }
}