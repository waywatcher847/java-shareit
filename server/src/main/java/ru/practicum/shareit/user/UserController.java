package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.user.UserDto;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping(path = "/internal/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto user) {
        log.info("Server: POST /users, user={}", user);
        UserDto createdUser = userService.create(user);
        return ResponseEntity
                .created(URI.create("/users/" + createdUser.getId()))
                .body(createdUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Integer id, @Valid @RequestBody UserDto user) {
        log.info("Server: PATCH /users/{id}, id={}, user={}", id, user);
        UserDto updatedUser = userService.update(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
        log.info("Server: GET /users/{id}, id={}", id);
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id) {
        log.info("Server: DELETE /users/{id}, id={}", id);
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}