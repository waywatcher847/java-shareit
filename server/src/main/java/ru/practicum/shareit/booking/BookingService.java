package ru.practicum.shareit.booking;


import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingDtoRequest;
import ru.practicum.common.booking.BookingState;

import java.util.List;

public interface BookingService {

    BookingDto create(BookingDtoRequest bookingDtoNew, Integer booker);

    BookingDto approve(Integer bookingId, Integer userId, Boolean decision);

    BookingDto getById(Integer bookingId, Integer userId);

    List<BookingDto> getUserBookings(Integer bookerId, BookingState bookingState);

    List<BookingDto> getOwnerBookings(Integer ownerId, BookingState bookingState);
}
