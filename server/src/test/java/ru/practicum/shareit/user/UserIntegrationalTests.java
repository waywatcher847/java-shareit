package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.user.UserDto;
import ru.practicum.common.user.UserDtoNew;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserIntegrationalTests {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_ValidUser_ReturnsSavedUser() {
        UserDtoNew newUser = UserDtoNew.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        UserDto savedUser = userService.create(newUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void create_DuplicateEmail_ThrowsConflictException() {
        userService.create(UserDtoNew.builder().name("User1").email("test@example.com").build());
        UserDtoNew user2 = UserDtoNew.builder().name("User2").email("test@example.com").build();

        assertThrows(ConflictException.class, () -> userService.create(user2));
    }

    @Test
    void create_DuplicateEmailCaseInsensitive_ThrowsConflictException() {
        userService.create(UserDtoNew.builder().name("User1").email("test@example.com").build());
        UserDtoNew user2 = UserDtoNew.builder().name("User2").email("TEST@EXAMPLE.COM").build();

        assertThrows(ConflictException.class, () -> userService.create(user2));
    }

    @Test
    void getAll_EmptyDatabase_ReturnsEmptyList() {
        List<UserDto> users = userService.getAll();
        assertThat(users).isEmpty();
    }

    @Test
    void getAll_NonEmptyDatabase_ReturnsAllUsers() {
        userService.create(UserDtoNew.builder().name("User1").email("u1@test.com").build());
        userService.create(UserDtoNew.builder().name("User2").email("u2@test.com").build());

        List<UserDto> users = userService.getAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void getById_ExistingUser_ReturnsUser() {
        UserDto created = userService.create(UserDtoNew.builder().name("User").email("u@test.com").build());

        UserDto found = userService.getById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("User");
    }

    @Test
    void getById_NonExistingUser_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.getById(999));
    }

    @Test
    void update_ValidFields_UpdatesUser() {
        UserDto created = userService.create(UserDtoNew.builder().name("OldName").email("old@test.com").build());
        UserDtoNew updateData = UserDtoNew.builder().name("NewName").email("new@test.com").build();

        UserDto updated = userService.update(updateData, created.getId());

        assertThat(updated.getName()).isEqualTo("NewName");
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void update_PartialUpdateNullName_UpdatesOnlyEmail() {
        UserDto created = userService.create(UserDtoNew.builder().name("OldName").email("old@test.com").build());
        UserDtoNew updateData = UserDtoNew.builder().email("new@test.com").build();

        UserDto updated = userService.update(updateData, created.getId());

        assertThat(updated.getName()).isEqualTo("OldName");
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void update_BlankName_DoesNotUpdateName() {
        UserDto created = userService.create(UserDtoNew.builder().name("OldName").email("old@test.com").build());
        UserDtoNew updateData = UserDtoNew.builder().name("   ").email("new@test.com").build();

        UserDto updated = userService.update(updateData, created.getId());

        assertThat(updated.getName()).isEqualTo("OldName");
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void update_NonExistingUser_ThrowsValidationException() {
        UserDtoNew updateData = UserDtoNew.builder().name("NewName").email("new@test.com").build();

        assertThrows(ValidationException.class, () -> userService.update(updateData, 999));
    }

    @Test
    void update_DuplicateEmail_ThrowsConflictException() {
        UserDto user1 = userService.create(UserDtoNew.builder().name("User1").email("u1@test.com").build());
        UserDto user2 = userService.create(UserDtoNew.builder().name("User2").email("u2@test.com").build());

        UserDtoNew updateData = UserDtoNew.builder().email("u1@test.com").build();

        assertThrows(ConflictException.class, () -> userService.update(updateData, user2.getId()));
    }

    @Test
    void delete_ExistingUser_DeletesUser() {
        UserDto created = userService.create(UserDtoNew.builder().name("User").email("u@test.com").build());

        userService.delete(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(created.getId()));
    }
}