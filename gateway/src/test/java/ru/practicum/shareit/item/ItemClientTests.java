package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.common.comment.CommentDtoRequest;
import ru.practicum.common.item.ItemDtoRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(ItemClient.class)
@TestPropertySource(properties = "shareit-server.url=http://localhost:8080")
class ItemClientTests {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void getItemById_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/items/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = itemClient.getItemById(1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void getItemByUserId_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/items"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = itemClient.getItemByUserId(1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void searchText_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/items/search?text=asdasd"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = itemClient.searchText("asdasd", 1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void create_shouldCallCorrectEndpoint() {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("asdd").description("asdasd").available(true).build();

        server.expect(requestTo("http://localhost:8080/internal/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = itemClient.create(1, dto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void update_shouldCallCorrectEndpoint() {
        ItemDtoRequest dto = ItemDtoRequest.builder().name("Updated").build();

        server.expect(requestTo("http://localhost:8080/internal/items/1"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = itemClient.update(1, 1, dto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void addComment_shouldCallCorrectEndpoint() {
        CommentDtoRequest dto = CommentDtoRequest.builder().text("asdasd").build();

        server.expect(requestTo("http://localhost:8080/internal/items/1/comment"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = itemClient.addComment(1, 1, dto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void deleteItem_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/items/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        var response = itemClient.deleteItem(1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void getItemDtoWithBookingsAndComments_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/items/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = itemClient.getItemDtoWithBookingsAndComments(1, 1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }
}