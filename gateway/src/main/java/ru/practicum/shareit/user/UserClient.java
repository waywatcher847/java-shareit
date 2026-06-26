package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.common.user.UserDtoNew;
import ru.practicum.shareit.client.BaseClient;

@Slf4j
@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/internal/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getUserList() {
        log.info("Request getUserList");
        return get("");
    }

    public ResponseEntity<Object> getUserDto(Integer id) {
        log.info("Request getUserDto id={}", id);
        return get("/" + id);
    }

    public ResponseEntity<Object> create(UserDtoNew userDto) {
        log.info("Request create user data: {}", userDto);
        return post("", userDto);
    }

    public ResponseEntity<Object> update(Integer userId, UserDtoNew userDto) {
        log.info("Request update user ID={}, data: {}", userId, userDto);
        return patch("/" + userId, userDto);
    }

    public ResponseEntity<Object> delete(Integer id) {
        log.info("Request delete user id={}", id);
        return delete("/" + id);
    }
}