package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserIntegrationalTests {
    private final UserService userService;


    @Test
    void createUser_WhenEmailAlreadyExists_ShouldThrowInternalServerException() {
        UserDto userDto1 = UserDto.builder()
                .name("Name2")
                .email("name2@mail.ru")
                .build();

        UserDto userDto2 = UserDto.builder()
                .name("Name3")
                .email("name2@mail.ru")
                .build();

        UserDto createdUser = userService.create(userDto1);
        Integer createdUserId = createdUser.getId();

        UserDto retrievedUser = userService.getUserById(createdUserId);
        assertThat(retrievedUser.getEmail()).isEqualTo(userDto1.getEmail());

        InternalServerException exception = assertThrows(InternalServerException.class,
                () -> userService.create(userDto2));

        assertEquals("User with email name2@mail.ru already exists", exception.getMessage());
    }

    @Test
    void createUser_WhenValidData_ShouldCreateAndRetrieveUser() {
        UserDto userDto = UserDto.builder()
                .name("Name1")
                .email("name1@mail.ru")
                .build();

        UserDto createdUser = userService.create(userDto);
        Integer createdUserId = createdUser.getId();

        UserDto retrievedUser = userService.getUserById(createdUserId);

        assertThat(retrievedUser.getId()).isEqualTo(createdUserId);
        assertThat(retrievedUser.getName()).isEqualTo(userDto.getName());
        assertThat(retrievedUser.getEmail()).isEqualTo(userDto.getEmail());
    }

    @Test
    void updateUser_WhenValidData_ShouldReturnUpdatedUser() {
        UserDto userDto = UserDto.builder()
                .name("Name4")
                .email("name4@mail.ru")
                .build();

        UserDto createdUser = userService.create(userDto);
        Integer createdUserId = createdUser.getId();

        UserDto updateDto = UserDto.builder()
                .name("UpdatedName")
                .email("updated@mail.ru")
                .build();

        UserDto updatedUser = userService.update(createdUserId, updateDto);

        assertThat(updatedUser.getId()).isEqualTo(createdUserId);
        assertThat(updatedUser.getName()).isEqualTo("UpdatedName");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@mail.ru");
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        Integer nonExistentId = 9999;

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(nonExistentId));

        assertEquals("User with ID 9999 not found", exception.getMessage());
    }

    @Test
    void updateUser_WhenEmailAlreadyExists_ShouldThrowInternalServerException() {
        UserDto user1 = UserDto.builder().name("User1").email("user1@mail.ru").build();
        UserDto user2 = UserDto.builder().name("User2").email("user2@mail.ru").build();

        userService.create(user1);
        UserDto createdUser2 = userService.create(user2);
        Integer userId2 = createdUser2.getId();

        UserDto updateDto = UserDto.builder()
                .email("user1@mail.ru")
                .build();

        InternalServerException exception = assertThrows(InternalServerException.class,
                () -> userService.update(userId2, updateDto));

        assertEquals("Email user1@mail.ru is already used", exception.getMessage());
    }

    @Test
    void updateUser_WhenIdMismatch_ShouldThrowInternalServerException() {
        UserDto userDto = UserDto.builder()
                .name("Name5")
                .email("name5@mail.ru")
                .build();

        UserDto createdUser = userService.create(userDto);
        Integer createdUserId = createdUser.getId();

        UserDto updateDto = UserDto.builder()
                .id(createdUserId + 1)
                .name("UpdatedName")
                .email("updated@mail.ru")
                .build();

        InternalServerException exception = assertThrows(InternalServerException.class,
                () -> userService.update(createdUserId, updateDto));

        assertEquals("ID in request =/= ID in URL", exception.getMessage());
    }

    @Test
    void deleteUserById_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        Integer nonExistentId = 9998;

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteUserById(nonExistentId));

        assertEquals("User with ID 9998 not found", exception.getMessage());
    }

    @Test
    void deleteUserById_WhenUserExists_ShouldDeleteUser() {
        UserDto userDto = UserDto.builder()
                .name("Name6")
                .email("name6@mail.ru")
                .build();

        UserDto createdUser = userService.create(userDto);
        Integer createdUserId = createdUser.getId();

        userService.deleteUserById(createdUserId);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(createdUserId));

        assertEquals("User with ID " + createdUserId + " not found", exception.getMessage());
    }
}