package ru.practicum.shareit.booking;


import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingRequestDto;

import java.util.List;

public interface BookingService {

    BookingDto create(BookingRequestDto bookingRequestDto, Integer userId);

    BookingDto approve(Integer bookingId, Boolean approved, Integer userId);

    BookingDto getById(Integer bookingId, Integer userId);

    List<BookingDto> getUserBookings(Integer userId, String state, Integer from, Integer size);

    List<BookingDto> getOwnerBookings(Integer ownerId, String state, Integer from, Integer size);
}
