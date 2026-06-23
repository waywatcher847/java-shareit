package ru.practicum.shareit.user;

import ru.practicum.common.user.UserDto;

public interface UserService {

    UserDto create(UserDto user);

    UserDto update(Integer id, UserDto userDto);

    UserDto getUserById(Integer id);

    void deleteUserById(Integer id);
}
