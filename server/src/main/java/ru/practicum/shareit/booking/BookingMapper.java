package ru.practicum.shareit.booking;

import org.mapstruct.*;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "start", source = "startDate")
    @Mapping(target = "end", source = "endDate")
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "item", ignore = true)
    BookingDto toDto(Booking booking);

    @Mapping(target = "startDate", source = "start")
    @Mapping(target = "endDate", source = "end")
    @Mapping(target = "userId", source = "booker.id")
    @Mapping(target = "itemId", source = "item.id")
    Booking toEntity(BookingDto bookingDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "startDate", source = "start")
    @Mapping(target = "endDate", source = "end")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "status", ignore = true)
    Booking toEntity(BookingRequestDto requestBookingDto);

    List<BookingDto> toDtoList(List<Booking> bookings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "itemId", ignore = true)
    void updateBookingFromDto(BookingDto bookingDto, @MappingTarget Booking booking);
}