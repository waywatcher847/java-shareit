package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByUserIdOrderByStartDateDesc(Integer userId, Pageable pageable);

    List<Booking> findByUserIdAndStatusOrderByStartDateDesc(Integer userId, BookingStatus status, Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE user_id = :userId " +
            "AND start_date <= :now AND end_date >= :now " +
            "ORDER BY start_date DESC",
            nativeQuery = true)
    List<Booking> findCurrentByBookerId(@Param("userId") Integer userId,
                                        @Param("now") LocalDateTime now,
                                        Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE user_id = :userId " +
            "AND end_date < :now " +
            "ORDER BY start_date DESC",
            nativeQuery = true)
    List<Booking> findPastByBookerId(@Param("userId") Integer userId,
                                     @Param("now") LocalDateTime now,
                                     Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE user_id = :userId " +
            "AND start_date > :now " +
            "ORDER BY start_date DESC",
            nativeQuery = true)
    List<Booking> findFutureByBookerId(@Param("userId") Integer userId,
                                       @Param("now") LocalDateTime now,
                                       Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON b.item_id = i.id " +
            "WHERE i.user_id = :ownerId " +
            "ORDER BY b.start_date DESC",
            nativeQuery = true)
    List<Booking> findByItemOwnerIdOrderByStartDesc(@Param("ownerId") Integer ownerId, Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON b.item_id = i.id " +
            "WHERE i.user_id = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.start_date DESC",
            nativeQuery = true)
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(@Param("ownerId") Integer ownerId,
                                                             @Param("status") String status,
                                                             Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON b.item_id = i.id " +
            "WHERE i.user_id = :ownerId " +
            "AND b.start_date <= :now AND b.end_date >= :now " +
            "ORDER BY b.start_date DESC",
            nativeQuery = true)
    List<Booking> findCurrentByOwnerId(@Param("ownerId") Integer ownerId,
                                       @Param("now") LocalDateTime now,
                                       Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON b.item_id = i.id " +
            "WHERE i.user_id = :ownerId " +
            "AND b.end_date < :now " +
            "ORDER BY b.start_date DESC",
            nativeQuery = true)
    List<Booking> findPastByOwnerId(@Param("ownerId") Integer ownerId,
                                    @Param("now") LocalDateTime now,
                                    Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON b.item_id = i.id " +
            "WHERE i.user_id = :ownerId " +
            "AND b.start_date > :now " +
            "ORDER BY b.start_date DESC",
            nativeQuery = true)
    List<Booking> findFutureByOwnerId(@Param("ownerId") Integer ownerId,
                                      @Param("now") LocalDateTime now,
                                      Pageable pageable);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM bookings WHERE item_id = :itemId " +
            "AND status = 'APPROVED' " +
            "AND (:start BETWEEN start_date AND end_date OR " +
            ":end BETWEEN start_date AND end_date OR " +
            "(start_date <= :start AND end_date >= :end))",
            nativeQuery = true)
    boolean existsApprovedBookingsForItemBetweenDates(
            @Param("itemId") Integer itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT * FROM bookings WHERE item_id = :itemId " +
            "AND user_id = :userId " +
            "AND status = 'APPROVED' " +
            "AND end_date < :now " +
            "ORDER BY end_date DESC",
            nativeQuery = true)
    List<Booking> findLastBookingForItem(
            @Param("itemId") Integer itemId,
            @Param("userId") Integer userId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE item_id = :itemId " +
            "AND status = 'APPROVED' " +
            "AND start_date > :now " +
            "ORDER BY start_date ASC",
            nativeQuery = true)
    List<Booking> findNextBookingForItem(
            @Param("itemId") Integer itemId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    List<Booking> findByUserIdAndItemIdAndStatus(Integer userId, Integer itemId, BookingStatus status);
}