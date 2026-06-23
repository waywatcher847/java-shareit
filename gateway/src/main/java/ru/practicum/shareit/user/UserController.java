package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.user.UserDto;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getUserDto(@PathVariable Integer id) {
        log.info("Gateway: GET /users/{}", id);
        return userClient.getUserDto(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getUserList() {
        log.info("Gateway: GET /users");
        return userClient.getUserList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestBody @Validated(UserDto.Create.class) UserDto user) {
        log.info("Gateway: POST /users");
        return userClient.create(user);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@PathVariable Integer id,
                                         @RequestBody @Validated(UserDto.Edit.class) UserDto user) {
        log.info("Gateway: PATCH /users/{}", id);
        return userClient.update(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        log.info("Gateway: DELETE /users/{}", id);
        return userClient.delete(id);
    }
}