package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.Constants;
import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.common.request.ItemRequestDtoResponse;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void createRequest_shouldReturn201() throws Exception {
        ItemRequestDto request = new ItemRequestDto();
        request.setDescription("qweqweqwe");

        ItemRequestDtoResponse response = new ItemRequestDtoResponse();
        response.setId(1);
        response.setDescription("qweqweqwe");

        when(requestService.createRequest(eq(1), any(ItemRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/internal/requests")
                        .header(Constants.USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getRequestById_shouldReturn200() throws Exception {
        ItemRequestDtoResponse response = new ItemRequestDtoResponse();
        response.setId(1);

        when(requestService.getRequestById(eq(1), eq(1))).thenReturn(response);

        mockMvc.perform(get("/internal/requests/1")
                        .header(Constants.USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUserRequests_shouldReturn200() throws Exception {
        ItemRequestDtoResponse req1 = new ItemRequestDtoResponse();
        req1.setId(1);
        req1.setDescription("zxczxczxc");
        req1.setCreated(LocalDateTime.now());

        ItemRequestDtoResponse req2 = new ItemRequestDtoResponse();
        req2.setId(2);
        req2.setDescription("asdasdr");
        req2.setCreated(LocalDateTime.now());

        when(requestService.getUserRequests(eq(1)))
                .thenReturn(List.of(req1, req2));

        mockMvc.perform(get("/internal/requests")
                        .header(Constants.USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("zxczxczxc"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getUserRequests_emptyList_shouldReturn200() throws Exception {
        when(requestService.getUserRequests(eq(1)))
                .thenReturn(List.of());

        mockMvc.perform(get("/internal/requests")
                        .header(Constants.USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUserRequests_withoutUserIdHeader_shouldReturn400() throws Exception {
        mockMvc.perform(get("/internal/requests"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getAllRequests_shouldReturn200() throws Exception {
        ItemRequestDtoResponse req1 = new ItemRequestDtoResponse();
        req1.setId(10);
        req1.setDescription("fghfghfgh");

        ItemRequestDtoResponse req2 = new ItemRequestDtoResponse();
        req2.setId(11);
        req2.setDescription("Need a hammer");

        when(requestService.getAllRequests(eq(5)))
                .thenReturn(List.of(req1, req2));

        mockMvc.perform(get("/internal/requests/all")
                        .header(Constants.USER_ID_HEADER, 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].description").value("fghfghfgh"))
                .andExpect(jsonPath("$[1].id").value(11));
    }

    @Test
    void getAllRequests_emptyList_shouldReturn200() throws Exception {
        when(requestService.getAllRequests(eq(5)))
                .thenReturn(List.of());

        mockMvc.perform(get("/internal/requests/all")
                        .header(Constants.USER_ID_HEADER, 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllRequests_withoutUserIdHeader_shouldReturn400() throws Exception {
        mockMvc.perform(get("/internal/requests/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_notFound_shouldReturn404() throws Exception {
        when(requestService.getRequestById(eq(1), eq(999)))
                .thenThrow(new NotFoundException("Request with id=999 not found"));

        mockMvc.perform(get("/internal/requests/999")
                        .header(Constants.USER_ID_HEADER, 1))
                .andExpect(status().isNotFound());
    }
}
