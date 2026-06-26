package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentDtoRequest;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.item.ItemDtoOwner;
import ru.practicum.common.item.ItemDtoRequest;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.common.Constants.USER_ID_HEADER;

@WebMvcTest(ItemController.class)
class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_shouldReturn201() throws Exception {
        ItemDtoRequest request = new ItemDtoRequest();
        request.setName("DDD");
        request.setDescription("Powerful DDD");
        request.setAvailable(true);

        ItemDto createdItem = new ItemDto();
        createdItem.setId(1);
        createdItem.setName("DDD");

        when(itemService.create(any(ItemDtoRequest.class), eq(1))).thenReturn(createdItem);

        mockMvc.perform(post("/internal/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("DDD"));
    }

    @Test
    void addComment_shouldReturn200() throws Exception {
        CommentDtoRequest request = new CommentDtoRequest();
        request.setText("!!!!!!");

        CommentDto commentDto = new CommentDto();
        commentDto.setId(1);
        commentDto.setText("!!!!!!");

        when(itemService.addComment(any(CommentDtoRequest.class), eq(1), eq(1))).thenReturn(commentDto);

        mockMvc.perform(post("/internal/items/1/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("!!!!!!"));
    }

    @Test
    void addComment_emptyText_shouldReturn400() throws Exception {
        CommentDtoRequest request = new CommentDtoRequest();

        mockMvc.perform(post("/internal/items/1/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        ItemDtoRequest request = new ItemDtoRequest();
        request.setName("upd DDD");
        request.setDescription("upd description");
        request.setAvailable(true);

        ItemDto updatedItem = new ItemDto();
        updatedItem.setId(5);
        updatedItem.setName("upd DDD");

        when(itemService.update(any(ItemDtoRequest.class), eq(5), eq(1)))
                .thenReturn(updatedItem);

        mockMvc.perform(patch("/internal/items/5")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("upd DDD"));
    }

    @Test
    void update_withoutUserIdHeader_shouldReturn400() throws Exception {
        ItemDtoRequest request = new ItemDtoRequest();
        request.setName("DDD");

        mockMvc.perform(patch("/internal/items/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getItemById_shouldReturn200() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(7);
        item.setName("DDD");

        when(itemService.getById(eq(7), eq(1))).thenReturn(item);

        mockMvc.perform(get("/internal/items/7")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("DDD"));
    }

    @Test
    void getItemById_withoutUserIdHeader_shouldReturn400() throws Exception {
        mockMvc.perform(get("/internal/items/7"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void searchItem_withText_shouldReturn200() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(3);
        item.setName("DDD");

        when(itemService.getByText(eq("DDD"), eq(1)))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/internal/items/search")
                        .header(USER_ID_HEADER, 1)
                        .param("text", "DDD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void searchItem_emptyResult_shouldReturn200() throws Exception {
        when(itemService.getByText(eq("nonexistent"), eq(1)))
                .thenReturn(List.of());

        mockMvc.perform(get("/internal/items/search")
                        .header(USER_ID_HEADER, 1)
                        .param("text", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchItem_withoutTextParam_shouldReturn400() throws Exception {
        mockMvc.perform(get("/internal/items/search")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItem_withoutUserIdHeader_shouldReturn400() throws Exception {
        mockMvc.perform(get("/internal/items/search")
                        .param("text", "DDD"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllItemsByUser_shouldReturn200() throws Exception {
        ItemDtoOwner item1 = new ItemDtoOwner();
        item1.setId(1);
        item1.setName("DDD");

        ItemDtoOwner item2 = new ItemDtoOwner();
        item2.setId(2);
        item2.setName("SSS");

        when(itemService.getUserItems(eq(1)))
                .thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/internal/items")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getAllItemsByUser_emptyList_shouldReturn200() throws Exception {
        when(itemService.getUserItems(eq(1)))
                .thenReturn(List.of());

        mockMvc.perform(get("/internal/items")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllItemsByUser_withoutUserIdHeader_shouldReturn400() throws Exception {
        mockMvc.perform(get("/internal/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_notFound_shouldReturn404() throws Exception {
        ItemDtoRequest request = new ItemDtoRequest();
        request.setName("upd Item");
        request.setDescription("upd description");
        request.setAvailable(true);

        when(itemService.update(any(ItemDtoRequest.class), eq(999), eq(1)))
                .thenThrow(new NotFoundException("Item with id=999 not found"));

        mockMvc.perform(patch("/internal/items/999")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_itemNotFound_shouldReturn404() throws Exception {
        CommentDtoRequest request = new CommentDtoRequest();
        request.setText("!!!!!!");

        when(itemService.addComment(any(CommentDtoRequest.class), eq(1), eq(999)))
                .thenThrow(new NotFoundException("Item with id=999 not found"));

        mockMvc.perform(post("/internal/items/999/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}