package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDtoNew;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(BookingDtoNew bookingDtoNew, Integer booker);

    BookingDto setBookingApproval(Integer bookingId, Integer userId, Boolean decision);

    BookingDto getBooking(Integer bookingId, Integer userId);

    List<BookingDto> getAllBookings(Integer bookerId, BookingState bookingState);

    List<BookingDto> getBookingsByOwner(Integer ownerId, BookingState bookingState);
}
