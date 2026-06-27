package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.common.user.UserDtoNew;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(UserClient.class)
@TestPropertySource(properties = "shareit-server.url=http://localhost:8080")
class UserClientTests {

    @Autowired
    private UserClient userClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void getUserDto_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"John\"}", MediaType.APPLICATION_JSON));

        var response = userClient.getUserDto(1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void getUserList_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/users"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = userClient.getUserList();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void create_shouldCallCorrectEndpoint() {
        UserDtoNew user = UserDtoNew.builder().name("John").email("john@test.com").build();

        server.expect(requestTo("http://localhost:8080/internal/users"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = userClient.create(user);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void update_shouldCallCorrectEndpoint() {
        UserDtoNew user = UserDtoNew.builder().name("Updated").build();

        server.expect(requestTo("http://localhost:8080/internal/users/1"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = userClient.update(1, user);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void delete_shouldCallCorrectEndpoint() {
        server.expect(requestTo("http://localhost:8080/internal/users/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        var response = userClient.delete(1);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }
}