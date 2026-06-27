package ru.practicum.common.item;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.common.user.UserDto;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoResponse {
    Integer itemId;
    String name;
    UserDto user;

}