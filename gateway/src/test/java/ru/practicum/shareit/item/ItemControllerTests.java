package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.comment.CommentDtoRequest;
import ru.practicum.common.item.ItemDtoRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.common.Constants.USER_ID_HEADER;

@WebMvcTest(ItemController.class)
class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;


    @Test
    void getItemById_shouldReturnOk() throws Exception {
        when(itemClient.getItemDtoWithBookingsAndComments(1, 1)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/1").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void getItemByUserId_shouldReturnOk() throws Exception {
        when(itemClient.getItemByUserId(1)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void searchText_shouldReturnOk() throws Exception {
        when(itemClient.searchText("asdasd", 1)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search").param("text", "asdasd").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void searchText_withoutTextParam_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withValidData_shouldReturnCreated() throws Exception {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("asdasd").description("ASDASDASD").available(true).build();
        when(itemClient.create(eq(1), any())).thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_withMissingName_shouldReturnBadRequest() throws Exception {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .description("No name").available(true).build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withEmptyDescription_shouldReturnBadRequest() throws Exception {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("Drill").description("").available(true).build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withDescriptionTooLong_shouldReturnBadRequest() throws Exception {
        String longDescription = "a".repeat(201);
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("Drill").description(longDescription).available(true).build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withNullAvailable_shouldReturnBadRequest() throws Exception {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("Drill").description("Good drill").build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        ItemDtoRequest dto = ItemDtoRequest.builder().name("Updated").build();
        when(itemClient.update(eq(1), eq(1), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_withValidData_shouldReturnOk() throws Exception {
        CommentDtoRequest dto = CommentDtoRequest.builder().text("ASDASD").build();
        when(itemClient.addComment(eq(1), eq(1), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_withEmptyContent_shouldReturnBadRequest() throws Exception {
        CommentDtoRequest dto = CommentDtoRequest.builder().text("").build();

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_withContentTooLong_shouldReturnBadRequest() throws Exception {
        String longContent = "a".repeat(201);
        CommentDtoRequest dto = CommentDtoRequest.builder().text(longContent).build();

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemById_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isBadRequest());
    }
}