package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.request.ItemRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.common.Constants.USER_ID_HEADER;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void getRequestById_shouldReturnOk() throws Exception {
        when(itemRequestClient.getRequestById(1, 10)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/10").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserRequests_shouldReturnOk() throws Exception {
        when(itemRequestClient.getUserRequests(1)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_shouldReturnOk() throws Exception {
        when(itemRequestClient.getAllRequests(1)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void createRequest_withValidData_shouldReturnCreated() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder().description("куплю гараж").build();
        when(itemRequestClient.createRequest(eq(1), any())).thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createRequest_withBlankDescription_shouldReturnBadRequest() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder().description("").build();

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequest_withDescriptionTooLong_shouldReturnBadRequest() throws Exception {
        String longDescription = "a".repeat(201);
        ItemRequestDto dto = ItemRequestDto.builder().description(longDescription).build();

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequest_withNullDescription_shouldReturnBadRequest() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder().description(null).build();

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}