package ru.practicum.common.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.comment.CommentDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Integer id;
    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotBlank(message = "description is mandatory")
    @Size(max = 200, message = "Description max length os 200")
    private String description;
    @NotNull(message = "available is mandatory")
    private Boolean available;
    private Integer userId;
    @JsonProperty("requestId")
    private Integer requestId;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private List<CommentDto> comments;
}