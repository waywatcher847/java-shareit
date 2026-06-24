package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentMapper;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTests {
    private final CommentMapper mapper = Mappers.getMapper(CommentMapper.class);

    @Test
    void toDto_ShouldMapFieldsAndUseDefaultMethodForAuthorName() {
        Comment comment = Comment.builder()
                .id(10)
                .text("Great item!")
                .itemId(5)
                .userId(2)
                .created(Instant.now())
                .build();

        CommentDto dto = mapper.toDto(comment);

        assertEquals(10, dto.getId());
        assertEquals("Great item!", dto.getText());
        assertEquals(5, dto.getItemId());
        assertEquals(2, dto.getUserId());
        assertNull(dto.getAuthorName());
    }

    @Test
    void toEntityFromDto_ShouldMapTextAndIdsIgnoreOthers() {
        CommentDto dto = CommentDto.builder()
                .id(1)
                .text("Review")
                .itemId(3)
                .userId(4)
                .build();

        Comment comment = mapper.toEntity(dto);

        assertEquals(dto.getText(), comment.getText());
        assertEquals(dto.getItemId(), comment.getItemId());
        assertEquals(dto.getUserId(), comment.getUserId());
        assertNull(comment.getId(), "id is ignored in toEntity");
        assertNull(comment.getCreated(), "created is ignored in toEntity");
    }

    @Test
    void toEntityFromRequestDto_ShouldMapOnlyText() {
        CommentRequestDto request = new CommentRequestDto();
        request.setText("New comment");

        Comment comment = mapper.toEntity(request);

        assertEquals("New comment", comment.getText());
        assertNull(comment.getId());
        assertNull(comment.getItemId());
        assertNull(comment.getUserId());
        assertNull(comment.getCreated());
    }
}