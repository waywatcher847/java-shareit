package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.common.request.ItemRequestDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(ItemRequestClient.class)
@TestPropertySource(properties = "shareit-server.url=http://localhost:8080")
class ItemRequestClientTests {

    @Autowired
    private ItemRequestClient itemRequestClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void getRequestById_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/requests/10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":10}", MediaType.APPLICATION_JSON));

        var response = itemRequestClient.getRequestById(1, 10);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void getUserRequests_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/requests"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = itemRequestClient.getUserRequests(1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void getAllRequests_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/requests/all"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = itemRequestClient.getAllRequests(1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void createRequest_shouldCallCorrectEndpoint() {
        ItemRequestDto dto = ItemRequestDto.builder().description("asdasd").build();

        server.expect(requestTo("http://localhost:8080/internal/requests"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = itemRequestClient.createRequest(1, dto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }
}