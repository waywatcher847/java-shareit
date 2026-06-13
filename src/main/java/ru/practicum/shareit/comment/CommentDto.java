package ru.practicum.shareit.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CommentDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    @Size(max = 200, message = "max length os 200")
    private String text;
    private Integer itemId;
    private String authorName;
    private Integer authorId;
    private LocalDateTime created;
}
