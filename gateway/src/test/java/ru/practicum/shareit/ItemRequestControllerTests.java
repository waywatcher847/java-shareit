package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTests {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestClient itemRequestClient;


    @Test
    void itemRequestController_WhenDescriptionIsBlank_ReturnsBadRequest() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("     ");

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemRequestController_WhenCreatingItemRequest_ReturnsOkStatus() throws Exception {
        Integer userId = 1;

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Description");

        when(itemRequestClient.createRequest(userId, itemRequestDto))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(itemRequestDto));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void itemRequestController_WhenDescriptionIsEmpty_ReturnsBadRequest() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("");

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemRequestController_WhenDescriptionIsNull_ReturnsBadRequest() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription(null);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemRequestController_WhenGettingOwnItemRequests_ReturnsOkStatus() throws Exception {
        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void itemRequestController_WhenHeaderIsMissing_ReturnsBadRequest() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Description");

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemRequestController_WhenGettingItemRequestById_ReturnsOkStatus() throws Exception {
        Integer requestId = 1;

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void itemRequestController_WhenGettingAllItemRequests_ReturnsOkStatus() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }
}