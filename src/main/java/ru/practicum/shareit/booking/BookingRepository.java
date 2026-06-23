package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<Booking> findByBookerIdOrderByStartDesc(Integer bookerId, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStatusAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Integer bookerId, BookingStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStatusAndEndBeforeOrderByStartDesc(
            Integer bookerId, BookingStatus status, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStatusAndStartAfterOrderByStartDesc(
            Integer bookerId, BookingStatus status, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Integer bookerId, BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdOrderByStartDesc(Integer ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStatusAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Integer ownerId, BookingStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStatusAndEndBeforeOrderByStartDesc(
            Integer ownerId, BookingStatus status, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStatusAndStartAfterOrderByStartDesc(
            Integer ownerId, BookingStatus status, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            Integer ownerId, BookingStatus status, Pageable pageable);

    @Query("""
    select i.id as id, max(b.start) as lastBooking from Booking b
    join b.item as i
    where i.id in (:ids) and b.start < :now and b.status = :status
    group by i.id
    order by i.id desc
    """)
    List<BookingLast> getBookingsByLast(@Param("ids") List<Integer> ids,
                                        @Param("now") LocalDateTime now,
                                        @Param("status") BookingStatus status);

    @Query("""
    select i.id as id, min(b.start) as nextBooking from Booking b
    join b.item as i
    where i.id in (:ids) and b.start > :now and b.status = :status
    group by i.id
    order by i.id asc
    """)
    List<BookingNext> getBookingsByNext(@Param("ids") List<Integer> ids,
                                        @Param("now") LocalDateTime now,
                                        @Param("status") BookingStatus status);

    @EntityGraph(attributePaths = {"item", "booker"})
    Optional<Booking> findByBookerIdAndItemIdAndStatusAndEndBefore(
            Integer bookerId, Integer itemId, BookingStatus status, LocalDateTime now);
}