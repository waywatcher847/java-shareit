package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.common.item.ItemDto;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTests {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void itemController_WhenCreatingItem_ReturnsOkStatus() throws Exception {
        Integer userId = 1;

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        when(itemClient.create(userId, itemDto))
                .thenReturn(ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(itemDto));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void itemController_WhenNameIsNull_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(null);
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenNameIsBlank_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("      ");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenDescriptionIsBlank_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("   ");
        itemDto.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenNameIsEmpty_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenDescriptionIsEmpty_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("");
        itemDto.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenDescriptionIsNull_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription(null);
        itemDto.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenAvailableIsNull_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(null);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenEditingItem_ReturnsOkStatus() throws Exception {
        Integer userId = 1;
        Integer itemId = 1;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Name");
        updates.put("description", "Updated Description");
        updates.put("available", false);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Updated Name");
        itemDto.setDescription("Updated Description");
        itemDto.setAvailable(false);

        when(itemClient.update(itemId, userId, updates))
                .thenReturn(ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(itemDto));

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void itemController_WhenHeaderIsMissing_ReturnsBadRequest() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenGettingItems_ReturnsOkStatus() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void itemController_WhenGettingItem_ReturnsOkStatus() throws Exception {
        Integer itemId = 1;

        mvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void itemController_WhenSearchTextIsNull_ReturnsBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenSearchingItems_ReturnsOkStatus() throws Exception {
        String searchingSubstring = "Item";

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", searchingSubstring))
                .andExpect(status().isOk());
    }

    @Test
    void itemController_WhenSearchTextIsBlank_ReturnsBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "    "))
                .andExpect(status().isBadRequest());
    }


    @Test
    void itemController_WhenSearchTextIsEmpty_ReturnsBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenCommentTextIsNull_ReturnsBadRequest() throws Exception {
        Integer itemId = 1;

        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText(null);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenCreatingComment_ReturnsOkStatus() throws Exception {
        Integer authorId = 1;
        Integer itemId = 1;

        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("Text");

        when(itemClient.addComment(authorId, itemId, commentDto))
                .thenReturn(ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(commentDto));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(commentDto.getText())));
    }

    @Test
    void itemController_WhenCommentTextIsEmpty_ReturnsBadRequest() throws Exception {
        Integer itemId = 1;

        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("");

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemController_WhenCommentTextIsBlank_ReturnsBadRequest() throws Exception {
        Integer itemId = 1;

        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("   ");

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }
}