package ru.practicum.shareit.user;

import org.mapstruct.Mapper;
import ru.practicum.common.user.UserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto userDto);

}
