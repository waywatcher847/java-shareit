package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingLast;
import ru.practicum.shareit.booking.model.BookingNext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    boolean existsByItemIdAndStatusAndEndGreaterThanEqualAndStartLessThanEqual(
            Integer itemId, BookingStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    Optional<Booking> findById(Integer bookingId);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdOrderByStartDesc(Integer bookerId);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStatusAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Integer bookerId, BookingStatus status, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStatusAndEndBeforeOrderByStartDesc(
            Integer bookerId, BookingStatus status, LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStatusAndStartAfterOrderByStartDesc(
            Integer bookerId, BookingStatus status, LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Integer bookerId, BookingStatus status);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdOrderByStartDesc(Integer ownerId);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStatusAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Integer ownerId, BookingStatus status, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStatusAndEndBeforeOrderByStartDesc(
            Integer ownerId, BookingStatus status, LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStatusAndStartAfterOrderByStartDesc(
            Integer ownerId, BookingStatus status, LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            Integer ownerId, BookingStatus status);

    @Query("SELECT b.item.id AS id, b AS lastBooking " +
            "FROM Booking b " +
            "WHERE b.id IN " +
            "  (SELECT MAX(b2.id) FROM Booking b2 " +
            "   WHERE b2.item.id IN :itemIds " +
            "     AND b2.status = :status " +
            "     AND b2.start <= :now " +
            "   GROUP BY b2.item.id)")
    List<BookingLast> getBookingsByLast(@Param("itemIds") List<Integer> itemIds,
                                        @Param("now") LocalDateTime now,
                                        @Param("status") BookingStatus status);

    @Query("SELECT b.item.id AS id, b AS nextBooking " +
            "FROM Booking b " +
            "WHERE b.id IN " +
            "  (SELECT MIN(b2.id) FROM Booking b2 " +
            "   WHERE b2.item.id IN :itemIds " +
            "     AND b2.status = :status " +
            "     AND b2.start >= :now " +
            "   GROUP BY b2.item.id)")
    List<BookingNext> getBookingsByNext(@Param("itemIds") List<Integer> itemIds,
                                        @Param("now") LocalDateTime now,
                                        @Param("status") BookingStatus status);

    @EntityGraph(attributePaths = {"item", "booker"})
    Optional<Booking> findByBookerIdAndItemIdAndStatusAndEndBefore(
            Integer bookerId, Integer itemId, BookingStatus status, LocalDateTime now);
}