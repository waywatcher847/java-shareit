package ru.practicum.common.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemDtoRequest {

    private Integer id;
    @NotBlank(groups = ItemDtoRequest.Create.class, message = "Name is mandatory")
    private String name;
    @NotBlank(groups = ItemDtoRequest.Create.class, message = "description is mandatory")
    @Size(groups = ItemDtoRequest.Create.class, max = 200, message = "Description max length os 200")
    private String description;
    @NotNull(groups = ItemDtoRequest.Create.class, message = "available is mandatory")
    private Boolean available;

    private Integer requestId;

    public interface Create {
    }
}
