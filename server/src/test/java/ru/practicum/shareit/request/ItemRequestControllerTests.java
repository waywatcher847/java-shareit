package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void getUserRequests_WhenUserHasRequests_ReturnsOk() throws Exception {
        Integer userId = 1;

        ItemRequestDto responseDto = new ItemRequestDto();
        responseDto.setId(1);
        responseDto.setDescription("My own request");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(Collections.emptyList());

        when(requestService.getUserRequests(userId)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/internal/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("My own request"));
    }

    @Test
    void getRequestById_WhenRequestExists_ReturnsOk() throws Exception {
        Integer userId = 1;
        Integer requestId = 10;

        ItemRequestDto responseDto = new ItemRequestDto();
        responseDto.setId(requestId);
        responseDto.setDescription("Need a drill");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(Collections.emptyList());

        when(requestService.getRequestById(userId, requestId)).thenReturn(responseDto);

        mockMvc.perform(get("/internal/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void createRequest_WhenValidRequest_ReturnsCreated() throws Exception {
        Integer userId = 1;

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill for the weekend");

        ItemRequestDto responseDto = new ItemRequestDto();
        responseDto.setId(1);
        responseDto.setDescription("Need a drill for the weekend");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(Collections.emptyList());

        when(requestService.createRequest(eq(userId), any(ItemRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/internal/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill for the weekend"))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void getAllRequests_WhenRequestsExist_ReturnsOk() throws Exception {
        Integer userId = 1;
        Integer from = 0;
        Integer size = 10;

        ItemRequestDto responseDto = new ItemRequestDto();
        responseDto.setId(2);
        responseDto.setDescription("Someone else's request");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(Collections.emptyList());

        when(requestService.getAllRequests(userId, from, size)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/internal/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].description").value("Someone else's request"));
    }

    @Test
    void getRequestById_WhenRequestDoesNotExist_ReturnsNotFound() throws Exception {
        Integer userId = 1;
        Integer requestId = 999;

        when(requestService.getRequestById(userId, requestId))
                .thenThrow(new NotFoundException("Item id 999 not found"));

        mockMvc.perform(get("/internal/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestById_WhenHeaderIsMissing_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/internal/requests/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_WhenInvalidFromType_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/internal/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "invalid")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
}