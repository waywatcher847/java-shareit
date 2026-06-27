package ru.practicum.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDto {

    @NotBlank(message = "description is mandatory")
    @Size(max = 200, message = "Description max length os 200")
    String description;
}