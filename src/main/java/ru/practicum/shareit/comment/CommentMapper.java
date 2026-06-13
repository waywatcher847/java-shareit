package ru.practicum.shareit.comment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .itemId(comment.getItem().getId())
                .authorName(comment.getAuthor().getName())
                .authorId(comment.getAuthor().getId())
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(CommentDto commentDto, User author, Item item) {
        return Comment.builder().text(commentDto.getText()).author(author).item(item).created(LocalDateTime.now()).build();
    }
}
