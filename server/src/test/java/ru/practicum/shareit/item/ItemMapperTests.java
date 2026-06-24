package ru.practicum.shareit.item;


import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.item.ItemDto;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTests {
    private final ItemMapper mapper = Mappers.getMapper(ItemMapper.class);

    @Test
    void toDto_ShouldMapBasicFieldsAndIgnoreComplexOnes() {
        Item item = Item.builder()
                .id(1).name("Chair").description("Wooden").available(true)
                .userId(2).requestId(3).comments(Collections.emptyList())
                .build();

        ItemDto dto = mapper.toDto(item);

        assertEquals(1, dto.getId());
        assertEquals("Chair", dto.getName());
        assertEquals("Wooden", dto.getDescription());
        assertEquals(true, dto.getAvailable());
        assertEquals(2, dto.getUserId());
        assertEquals(3, dto.getRequestId());
        assertNull(dto.getComments());
        assertNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
    }

    @Test
    void toEntityFromDto_ShouldMapFields() {
        ItemDto dto = ItemDto.builder()
                .id(1).name("Table").description("Glass").available(false)
                .userId(5).requestId(null)
                .build();

        Item item = mapper.toEntity(dto);

        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
        assertEquals(dto.getAvailable(), item.getAvailable());
        assertEquals(dto.getUserId(), item.getUserId());
        assertNull(item.getComments());
    }

    @Test
    void toDtoWithDetails_ShouldMapAllParameters() {
        Item item = Item.builder().id(1).name("Lamp").build();
        BookingDto lastBooking = BookingDto.builder().id(10).build();
        BookingDto nextBooking = BookingDto.builder().id(11).build();
        List<CommentDto> comments = List.of(CommentDto.builder().id(1).build());

        ItemDto dto = mapper.toDto(item, lastBooking, nextBooking, comments);

        assertEquals(1, dto.getId());
        assertEquals("Lamp", dto.getName());
        assertEquals(10, dto.getLastBooking().getId());
        assertEquals(11, dto.getNextBooking().getId());
        assertEquals(1, dto.getComments().getFirst().getId());
    }

    @Test
    void updateItemFromDto_ShouldUpdateNonNullAndIgnoreNulls() {
        Item original = Item.builder()
                .id(1).name("Old").description("OldDesc").available(true)
                .userId(2).requestId(3)
                .build();

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New");
        updateDto.setDescription(null);
        updateDto.setAvailable(false);

        mapper.updateItemFromDto(updateDto, original);

        assertEquals("New", original.getName());
        assertEquals("OldDesc", original.getDescription(), "description unchanged");
        assertEquals(false, original.getAvailable());
        assertEquals(1, original.getId());
    }
}