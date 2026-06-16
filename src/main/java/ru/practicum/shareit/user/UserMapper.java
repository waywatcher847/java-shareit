package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static UserDto mapToUserDto(ru.practicum.shareit.user.model.User user) {

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User mapToUser(UserDtoNew newUserRequest) {

        return User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
    }

    public static User updateUserField(User user, UserDtoUpdate updateUserDto) {
        if (updateUserDto.hasName()) {
            user.setName(updateUserDto.getName());
        }

        if (updateUserDto.hasEmail()) {
            user.setEmail(updateUserDto.getEmail());
        }

        return user;
    }
}
