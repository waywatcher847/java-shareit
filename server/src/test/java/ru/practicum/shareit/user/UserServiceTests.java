package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_WhenValidRequest_ShouldReturnCreatedUser() {
        UserDto request = formUserDto();
        UserDto expectedUser = formExpectedUser();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        User savedUser = formUser();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toEntity(request)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expectedUser);

        UserDto result = userService.create(request);

        assertThat(result, equalTo(expectedUser));
    }

    @Test
    void updateUser_WhenValidRequest_ShouldReturnUpdatedUser() {
        Integer userId = 1;
        UserDto request = formUserDto();
        User editingUser = formUserToEdit();
        UserDto expectedUser = formExpectedUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(editingUser));

        User savedUser = formUser();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expectedUser);

        UserDto result = userService.update(userId, request);

        assertThat(result, equalTo(expectedUser));
    }

    @Test
    void createUser_WhenEmailAlreadyExists_ShouldThrowInternalServerException() {
        UserDto request = formUserDto();
        User existingUser = formUser();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));

        final InternalServerException exception = assertThrows(InternalServerException.class,
                () -> userService.create(request));

        assertEquals("User with email " + request.getEmail() + " already exists", exception.getMessage());
    }

    @Test
    void updateUser_WhenEmailAlreadyExists_ShouldThrowInternalServerException() {
        Integer userId = 1;
        UserDto request = formUserDto();
        User editingUser = formUserToEdit();

        when(userRepository.findById(userId)).thenReturn(Optional.of(editingUser));

        User anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setEmail(request.getEmail());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(anotherUser));

        final InternalServerException exception = assertThrows(InternalServerException.class,
                () -> userService.update(userId, request));

        assertEquals("Email " + request.getEmail() + " is already used", exception.getMessage());
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        Integer userId = 999;
        UserDto request = formUserDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.update(userId, request));

        assertEquals("User with ID " + userId + " not found", exception.getMessage());
    }

    @Test
    void deleteUserById_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        Integer userId = 999;

        when(userRepository.existsById(userId)).thenReturn(false);

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteUserById(userId));

        assertEquals("User with ID " + userId + " not found", exception.getMessage());
    }

    @Test
    void deleteUserById_WhenUserExists_ShouldDeleteUser() {
        Integer userId = 1;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUserById(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        Integer userId = 999;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(userId));

        assertEquals("User with ID " + userId + " not found", exception.getMessage());
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        Integer userId = 1;
        UserDto expectedUser = formExpectedUser();

        User user = formUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedUser);

        UserDto result = userService.getUserById(userId);

        assertThat(result, equalTo(expectedUser));
    }

    @Test
    void updateUser_WhenNameIsNull_ShouldNotUpdateName() {
        Integer userId = 1;
        UserDto request = new UserDto();
        request.setEmail("newemail@mail.ru");
        User editingUser = formUserToEdit();
        when(userRepository.findById(userId)).thenReturn(Optional.of(editingUser));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        User savedUser = formUser();
        savedUser.setEmail("newemail@mail.ru");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(formExpectedUser());

        UserDto result = userService.update(userId, request);
        assertNotNull(result);
    }

    @Test
    void updateUser_WhenEmailIsNull_ShouldNotUpdateEmail() {
        Integer userId = 1;
        UserDto request = new UserDto();
        request.setName("NewName");
        User editingUser = formUserToEdit();
        when(userRepository.findById(userId)).thenReturn(Optional.of(editingUser));
        User savedUser = formUser();
        savedUser.setName("NewName");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(formExpectedUser());

        UserDto result = userService.update(userId, request);
        assertNotNull(result);
    }

    private User formUser() {
        User user = new User();
        user.setId(1);
        user.setName("Name");
        user.setEmail("name@mail.ru");
        return user;
    }

    private UserDto formUserDto() {
        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("name@mail.ru");
        return userDto;
    }

    private User formUserToEdit() {
        User user = new User();
        user.setId(1);
        user.setName("Name2");
        user.setEmail("name2@mail.ru");
        return user;
    }

    private UserDto formExpectedUser() {
        UserDto userDto = new UserDto();
        userDto.setId(1);
        userDto.setName("Name");
        userDto.setEmail("name@mail.ru");
        return userDto;
    }
}