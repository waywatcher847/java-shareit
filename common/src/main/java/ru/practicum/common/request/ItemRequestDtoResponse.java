package ru.practicum.common.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.common.item.ItemDtoResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDtoResponse {

    Integer id;
    String description;
    LocalDateTime created;
    List<ItemDtoResponse> items;
}