package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.common.user.UserDto;
import ru.practicum.common.user.UserDtoNew;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static UserDto mapToUserDto(User user) {

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

    public static User updateUserField(User user, UserDtoNew updateUserDto) {
        if (!(updateUserDto.getName() == null || updateUserDto.getName().isBlank())) {
            user.setName(updateUserDto.getName());
        }

        if (!(updateUserDto.getEmail() == null || updateUserDto.getEmail().isBlank())) {
            user.setEmail(updateUserDto.getEmail());
        }

        return user;
    }
}
