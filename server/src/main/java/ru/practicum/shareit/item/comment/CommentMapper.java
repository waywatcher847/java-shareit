package ru.practicum.shareit.item.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.shareit.user.UserRepository;

@Mapper(componentModel = "spring", uses = {UserRepository.class})
public interface CommentMapper {

    @Mapping(target = "authorName", source = "userId", qualifiedByName = "mapUserIdToName")
    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "userId", source = "userId")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "created", ignore = true)
    Comment toEntity(CommentDto commentDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "created", ignore = true)
    Comment toEntity(CommentRequestDto commentRequestDto);

    @Named("mapUserIdToName")
    default String mapUserIdToName(Integer userId) {
        return null;
    }
}