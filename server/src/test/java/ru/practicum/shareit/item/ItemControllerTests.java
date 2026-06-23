package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.common.item.ItemDto;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemService itemService;

    private ItemDto request;
    private ItemDto response;
    private ItemDto responseWithDetails;
    private CommentRequestDto commentRequest;
    private CommentDto commentResponse;

    @BeforeEach
    void setUp() {
        request = new ItemDto();
        request.setName("Item");
        request.setDescription("Description");
        request.setAvailable(true);

        response = new ItemDto();
        response.setId(1);
        response.setUserId(1);
        response.setName("Item");
        response.setDescription("Description");
        response.setAvailable(true);

        commentRequest = new CommentRequestDto();
        commentRequest.setText("Text");

        commentResponse = new CommentDto();
        commentResponse.setId(1);
        commentResponse.setText("Text");
        commentResponse.setItemId(1);
        commentResponse.setAuthorName("Author");
        commentResponse.setCreated(Instant.now());

        responseWithDetails = new ItemDto();
        responseWithDetails.setId(1);
        responseWithDetails.setUserId(1);
        responseWithDetails.setName("Item");
        responseWithDetails.setDescription("Description");
        responseWithDetails.setAvailable(true);

        BookingDto lastBooking = new BookingDto();
        lastBooking.setId(10);
        BookingDto nextBooking = new BookingDto();
        nextBooking.setId(11);

        responseWithDetails.setLastBooking(lastBooking);
        responseWithDetails.setNextBooking(nextBooking);
        responseWithDetails.setComments(List.of(commentResponse));
    }

    @Test
    void updateItem_WhenValidRequest_ReturnsUpdatedItem() throws Exception {
        Integer userId = 1;
        Integer itemId = 1;

        when(itemService.update(itemId, request, userId))
                .thenReturn(response);

        mvc.perform(patch("/internal/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Item"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void createItem_WhenValidRequest_ReturnsCreatedItem() throws Exception {
        Integer userId = 1;

        when(itemService.create(request, userId))
                .thenReturn(response);

        mvc.perform(post("/internal/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Item"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getAllItems_WhenUserHasItems_ReturnsItemsWithDetails() throws Exception {
        Integer userId = 1;

        when(itemService.getAllItemsByUser(userId))
                .thenReturn(List.of(responseWithDetails));

        mvc.perform(get("/internal/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("Item"))
                .andExpect(jsonPath("$.[0].description").value("Description"))
                .andExpect(jsonPath("$.[0].available").value(true))
                .andExpect(jsonPath("$.[0].lastBooking").exists())
                .andExpect(jsonPath("$.[0].nextBooking").exists())
                .andExpect(jsonPath("$.[0].comments").exists());
    }

    @Test
    void getItemById_WhenItemExists_ReturnsItemWithDetails() throws Exception {
        Integer userId = 1;
        Integer itemId = 1;

        when(itemService.getItemByIdWithDetails(itemId, userId))
                .thenReturn(responseWithDetails);

        mvc.perform(get("/internal/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Item"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.lastBooking").exists())
                .andExpect(jsonPath("$.nextBooking").exists())
                .andExpect(jsonPath("$.comments").exists());
    }

    @Test
    void createComment_WhenValidRequest_ReturnsCreatedComment() throws Exception {
        Integer userId = 1;
        Integer itemId = 1;

        when(itemService.addComment(userId, itemId, commentRequest))
                .thenReturn(commentResponse);

        mvc.perform(post("/internal/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Text"))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.authorName").value("Author"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void searchItems_WhenValidSearchString_ReturnsMatchingItems() throws Exception {
        Integer userId = 1;
        String searchingSubstring = "substring";

        when(itemService.searchItem(searchingSubstring, userId))
                .thenReturn(List.of(response));

        mvc.perform(get("/internal/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("text", searchingSubstring))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("Item"))
                .andExpect(jsonPath("$.[0].description").value("Description"))
                .andExpect(jsonPath("$.[0].available").value(true));
    }
}