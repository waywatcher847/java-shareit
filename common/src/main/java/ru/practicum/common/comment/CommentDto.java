package ru.practicum.common.comment;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {

    Integer id;
    String text;
    String authorName;
    Integer itemId;
    Integer userId;
    Instant created;
}