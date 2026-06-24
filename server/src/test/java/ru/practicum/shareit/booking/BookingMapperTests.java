package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTests {
    private final BookingMapper mapper = Mappers.getMapper(BookingMapper.class);

    @Test
    void toDto_ShouldMapDatesAndStatusAndIgnoreNestedObjects() {
        Booking booking = Booking.builder()
                .id(1)
                .startDate(LocalDateTime.of(2026, 6, 24, 10, 0))
                .endDate(LocalDateTime.of(2026, 6, 25, 10, 0))
                .userId(2)
                .itemId(3)
                .status(BookingStatus.APPROVED)
                .build();

        BookingDto dto = mapper.toDto(booking);

        assertEquals(1, dto.getId());
        assertEquals(booking.getStartDate(), dto.getStart());
        assertEquals(booking.getEndDate(), dto.getEnd());
        assertEquals(BookingStatus.APPROVED, dto.getStatus());
        assertNull(dto.getBooker(), "booker should be ignored");
        assertNull(dto.getItem(), "item should be ignored");
    }

    @Test
    void toEntityFromDto_ShouldMapDatesAndIds() {
        BookingDto dto = BookingDto.builder()
                .id(1)
                .start(LocalDateTime.of(2026, 7, 1, 12, 0))
                .end(LocalDateTime.of(2026, 7, 2, 12, 0))
                .build();

        Booking booking = mapper.toEntity(dto);

        assertEquals(dto.getStart(), booking.getStartDate());
        assertEquals(dto.getEnd(), booking.getEndDate());
    }

    @Test
    void toEntityFromRequestDto_ShouldMapOnlyDatesAndItemId() {
        BookingRequestDto request = BookingRequestDto.builder()
                .itemId(5)
                .start(LocalDateTime.of(2026, 8, 1, 14, 0))
                .end(LocalDateTime.of(2026, 8, 2, 14, 0))
                .build();

        Booking booking = mapper.toEntity(request);

        assertNull(booking.getId(), "id should be ignored");
        assertNull(booking.getUserId(), "userId should be ignored");
        assertNull(booking.getStatus(), "status should be ignored");
        assertEquals(request.getItemId(), booking.getItemId());
        assertEquals(request.getStart(), booking.getStartDate());
        assertEquals(request.getEnd(), booking.getEndDate());
    }
}