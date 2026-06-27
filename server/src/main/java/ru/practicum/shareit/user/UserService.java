package ru.practicum.shareit.user;

import ru.practicum.common.user.UserDto;
import ru.practicum.common.user.UserDtoNew;

import java.util.List;

public interface UserService {

    List<UserDto> getAll();

    UserDto getById(Integer userId);

    UserDto create(UserDtoNew userDtoNew);

    UserDto update(UserDtoNew updateUserDto, Integer userId);

    void delete(Integer userId);
}
