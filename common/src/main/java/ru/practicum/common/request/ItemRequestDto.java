package ru.practicum.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.common.item.ItemResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDto {

    Integer id;
    @NotBlank(message = "description is mandatory")
    @Size(max = 200, message = "Description max length os 200")
    String description;
    LocalDateTime created;
    List<ItemResponseDto> items;
}