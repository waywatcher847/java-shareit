package ru.practicum.shareit.user;


import java.util.List;

public interface UserService {

    List<UserDto> getAll();

    UserDto getById(Integer userId);

    UserDto create(UserDtoNew userDtoNew);

    UserDto update(UserDtoUpdate updateUserDto, Integer userId);

    void delete(Integer userId);
}
